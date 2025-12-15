package ssu.sokdak.compliment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ssu.sokdak.category.domain.Category;
import ssu.sokdak.category.dto.CategoryContext;
import ssu.sokdak.category.repository.CategoryRepository;
import ssu.sokdak.club.repository.ClubMemberRepository;
import ssu.sokdak.compliment.domain.Compliment;
import ssu.sokdak.compliment.domain.ComplimentTemplate;
import ssu.sokdak.compliment.dto.ComplimentGenerateResponse;
import ssu.sokdak.compliment.dto.ComplimentHistoryResponse;
import ssu.sokdak.compliment.dto.ComplimentSelectRequest;
import ssu.sokdak.compliment.repository.ComplimentRepository;
import ssu.sokdak.compliment.repository.ComplimentTemplateRepository;
import ssu.sokdak.user.domain.User;
import ssu.sokdak.user.repository.UserCategorySelectionRepository;
import ssu.sokdak.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ComplimentService {

    private final ComplimentTemplateRepository complimentTemplateRepository;
    private final CategoryRepository categoryRepository;
    private final ComplimentIndexService complimentIndexService;
    private final ClubMemberRepository clubMemberRepository;
    private final UserCategorySelectionRepository userCategorySelectionRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;
    private final ComplimentRepository complimentRepository;
    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;

    public void saveComplimentTemplate(String text, String reqCategory) {

        Category category = categoryRepository.findByCode(reqCategory)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."));

        ComplimentTemplate complimentTemplate = ComplimentTemplate.builder()
                .category(category)
                .text(text)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        complimentTemplateRepository.save(complimentTemplate);

        complimentIndexService.indexCompliment(complimentTemplate);
    }

    public List<ComplimentGenerateResponse> createCompliments(Long clubId, Long senderId) {

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리가 없습니다.");
        }

        // 동아리 후보 풀 (본인 제외)
        List<Long> pool = clubMemberRepository.findActiveMemberIdsExcluding(clubId, senderId);
        if (pool.size() < 4) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "후보가 4명 미만입니다.");

        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        Map<String, CategoryContext> ctxByCode = new LinkedHashMap<>();

        for (Category cat : categories) {
            List<Long> candidates = new ArrayList<>(pool);
            Collections.shuffle(candidates, new Random(rnd.nextLong()));
            candidates = candidates.subList(0, 4);

            Map<Long, String> nameById = userRepository.findByIdIn(candidates).stream()
                    .collect(Collectors.toMap(
                            UserRepository.UserNameView::getId,
                            UserRepository.UserNameView::getName
                    ));

            List<UserCategorySelectionRepository.UserOptionView> views =
                    userCategorySelectionRepository.findOptionLabelsByUsersAndCategory(candidates, cat.getId());

            Map<Long, String> optionLabelByUserId = views.stream()
                    .collect(Collectors.toMap(
                            UserCategorySelectionRepository.UserOptionView::getUserId,
                            UserCategorySelectionRepository.UserOptionView::getLabel
                    ));

            List<String> optionLabelsInOrder = candidates.stream()
                    .map(uid -> {
                        String label = optionLabelByUserId.get(uid);
                        return (label == null || label.isBlank()) ? "칭찬" : label; // fallback 키워드
                    })
                    .toList();

            CategoryContext ctx = CategoryContext.builder()
                    .category(cat)
                    .candidateIds(List.copyOf(candidates))
                    .nameById(nameById)
                    .optionLabelsInOrder(optionLabelsInOrder)
                    .build();

            ctxByCode.put(cat.getCode(), ctx);
        }

        Map<String, List<String>> keywordsByCategoryCode = ctxByCode.values().stream()
                .collect(Collectors.toMap(
                        ctx -> ctx.getCategory().getCode(),
                        CategoryContext::getOptionLabelsInOrder,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, String> generatedByCategory = generateLines(keywordsByCategoryCode);

        List<ComplimentGenerateResponse> responses = new ArrayList<>();

        for (CategoryContext ctx : ctxByCode.values()) {
            String categoryCode = ctx.getCategory().getCode();
            String gptLine = generatedByCategory.get(categoryCode);

            if (gptLine == null || gptLine.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "AI 응답에 카테고리 문장이 누락되었습니다: " + categoryCode);
            }

            SearchRequest req = SearchRequest.builder()
                    .query(gptLine)
                    .topK(5)
                    .filterExpression("category == '" + categoryCode.replace("'", "''") + "'")
                    .build();

            List<Document> docs = vectorStore.similaritySearch(req);
            if (docs.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 카테고리 템플릿 없음");

            Document picked = docs.get(rnd.nextInt(docs.size()));
            Object tid = picked.getMetadata().get("templateId");
            if (tid == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "템플릿 ID 메타데이터 누락");
            Long templateId = Long.valueOf(tid.toString());

            ComplimentTemplate template = complimentTemplateRepository.findById(templateId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"템플릿을 찾을 수 없습니다."));

            Compliment draft = Compliment.builder()
                    .sender(sender)
                    .receiver(null)
                    .category(ctx.getCategory())
                    .template(template)
                    .score(null)
                    .createdAt(LocalDateTime.now())
                    .build();

            Compliment saved = complimentRepository.save(draft);

            ComplimentGenerateResponse dto = new ComplimentGenerateResponse();
            dto.setClubId(clubId);
            dto.setSenderId(senderId);
            dto.setComplimentId(saved.getId());
            dto.setCategory(categoryCode);
            dto.setText(template.getText());

            List<ComplimentGenerateResponse.CandidateInfo> candInfos = ctx.getCandidateIds().stream()
                    .map(uid -> {
                        var ci = new ComplimentGenerateResponse.CandidateInfo();
                        ci.setUserId(uid);
                        ci.setUserName(ctx.getNameById().get(uid));
                        return ci;
                    })
                    .toList();

            dto.setCandidates(candInfos);

            responses.add(dto);
        }

        return responses;
    }


    public void selectComplimentUser(ComplimentSelectRequest request) {

        Compliment compliment = complimentRepository.findById(request.getComplimentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "칭찬을 찾을 수 없습니다."));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        compliment.updateReceiver(user);

        compliment.updateAnonymity(request.getAnonymity());
    }

    public List<ComplimentHistoryResponse> getSentCompliments(Long userId) {
        return complimentRepository.findSentByUserId(userId).stream()
                .map(c -> {
                    User receiver = c.getReceiver();
                    return ComplimentHistoryResponse.builder()
                            .complimentId(c.getId())
                            .userId(receiver.getId())
                            .name(receiver.getName())
                            .gender(receiver.getGender())
                            .message(c.getTemplate().getText())
                            .anonymity(c.getAnonymity())
                            .createdAt(c.getCreatedAt())
                            .build();
                })
                .toList();
    }

    public List<ComplimentHistoryResponse> getReceivedCompliments(Long userId) {
        return complimentRepository.findReceivedByUserId(userId).stream()
                .map(c -> {
                    boolean isAnonymous = Boolean.TRUE.equals(c.getAnonymity());
                    User sender = c.getSender();
                    if (isAnonymous) {
                        return ComplimentHistoryResponse.builder()
                                .complimentId(c.getId())
                                .userId(null)
                                .name("익명")
                                .gender(sender.getGender())
                                .message(c.getTemplate().getText())
                                .anonymity(true)
                                .createdAt(c.getCreatedAt())
                                .build();
                    }

                    return ComplimentHistoryResponse.builder()
                            .complimentId(c.getId())
                            .userId(sender.getId())
                            .name(sender.getName())
                            .gender(sender.getGender())
                            .message(c.getTemplate().getText())
                            .anonymity(false)
                            .createdAt(c.getCreatedAt())
                            .build();
                })
                .toList();
    }

    public Map<String, String> generateLines(Map<String, List<String>> keywordsByCategoryCode) {
        long t0 = System.currentTimeMillis();
        log.info("[AI] start generateLines categories={}", keywordsByCategoryCode.keySet());

        try {
            String inputJson = objectMapper.writeValueAsString(keywordsByCategoryCode);

            String prompt = """
            역할: 당신은 짧고 자연스러운 한국어 칭찬 문구를 만드는 도우미입니다.
            
            아래 입력 JSON의 각 키(카테고리 코드)에 대해, 값(키워드 4개)을 모두 반영한 칭찬 문장 1개씩 만들어주세요.
            
            제약:
            - 각 값은 1문장, 25자 내외
            - 특정 인물 이름 금지
            - 과장/비속어 금지
            - 키워드 4개 모두 자연스럽게 포함
            
            출력 규칙(매우 중요):
            - 출력은 JSON 객체 1개만
            - 첫 글자는 반드시 '{' 이고 마지막 글자는 반드시 '}' 여야 함
            - 코드블록(```), 마크다운, 설명문, 불릿, 번호, 줄바꿈 추가 금지
            - JSON 키는 입력의 카테고리 코드를 그대로 사용
            - JSON 값은 문장(문자열)만
            
            예시:
            {"LEADERSHIP":"...", "CREATIVITY":"..."}
            
            입력(JSON):
            %s
            """.formatted(inputJson);

            log.info("[AI] prompt chars={} build={}ms", prompt.length(), System.currentTimeMillis() - t0);

            String content;
            try {
                log.info("[AI] call() entering t={}ms", System.currentTimeMillis() - t0);
                content = chatClient.prompt(prompt).call().content();
                log.info("[AI] call() returned t={}ms", System.currentTimeMillis() - t0);
            } catch (Exception e) {
                log.error("[AI] call() exception t={}ms type={} msg={}",
                        System.currentTimeMillis() - t0,
                        e.getClass().getName(),
                        e.getMessage(),
                        e);
                throw e;
            }

            log.info("[AI] content length={} t={}ms",
                    content == null ? -1 : content.length(),
                    System.currentTimeMillis() - t0);

            log.info("[AI] raw content (first 500 chars)='{}'",
                    content == null ? "null"
                            : content.substring(0, Math.min(500, content.length())).replace("\n", "\\n"));

            if (content == null || content.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "AI 응답이 비어있습니다.");
            }

            String normalized = stripCodeFence(content);

            normalized = extractJsonObject(normalized);

            Map<String, String> parsed = objectMapper.readValue(
                    normalized,
                    new TypeReference<Map<String, String>>() {}
            );

            Map<String, String> normalizedParsed = parsed.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey() == null ? "" : e.getKey().trim(),
                            e -> e.getValue() == null ? "" : e.getValue().trim(),
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));

            Set<String> expected = keywordsByCategoryCode.keySet();
            if (!normalizedParsed.keySet().containsAll(expected)) {
                Set<String> missing = new LinkedHashSet<>(expected);
                missing.removeAll(normalizedParsed.keySet());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "AI JSON에 누락된 카테고리: " + missing + ", got=" + normalizedParsed.keySet());
            }

            log.info("[AI] parsed keys={} t={}ms", normalizedParsed.keySet(), System.currentTimeMillis() - t0);
            return normalizedParsed;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("[AI] generateLines failed t={}ms msg={}", System.currentTimeMillis() - t0, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "AI JSON 파싱 실패: " + e.getMessage(), e);
        }
    }

    private String stripCodeFence(String s) {
        String t = s.trim();
        if (t.startsWith("```")) {
            int firstNewline = t.indexOf('\n');
            if (firstNewline > 0) t = t.substring(firstNewline + 1);
            int lastFence = t.lastIndexOf("```");
            if (lastFence >= 0) t = t.substring(0, lastFence);
        }
        return t.trim();
    }

    private String extractJsonObject(String s) {
        String t = s.trim();

        int start = t.indexOf('{');
        int end = t.lastIndexOf('}');
        if (start < 0 || end < 0 || end <= start) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "AI 응답에서 JSON 객체를 찾지 못했습니다. raw=" + t.substring(0, Math.min(200, t.length()))
            );
        }
        return t.substring(start, end + 1).trim();
    }

}
