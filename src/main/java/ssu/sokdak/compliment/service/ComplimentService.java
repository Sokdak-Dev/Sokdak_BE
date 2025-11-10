package ssu.sokdak.compliment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ssu.sokdak.category.domain.Category;
import ssu.sokdak.category.repository.CategoryRepository;
import ssu.sokdak.club.repository.ClubMemberRepository;
import ssu.sokdak.compliment.domain.Compliment;
import ssu.sokdak.compliment.domain.ComplimentTemplate;
import ssu.sokdak.compliment.dto.ComplimentGenerateResponse;
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

        //sender 인스턴스
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        //카테고리 목록
        List<Category> categories = categoryRepository.findAll();

        // 동아리 후보 풀 (본인 제외)
        List<Long> pool = clubMemberRepository.findActiveMemberIdsExcluding(clubId, senderId);
        if (pool.size() < 4) throw new IllegalStateException("후보가 4명 미만입니다.");

        List<ComplimentGenerateResponse> responses = new ArrayList<>();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (Category cat : categories) {
            // 후보 4명 샘플링
            List<Long> candidates = new ArrayList<>(pool);
            Collections.shuffle(candidates, new Random(rnd.nextLong()));
            candidates = candidates.subList(0, 4);

            // 후보 4명의 이름 조회
            Map<Long, String> nameById = userRepository.findByIdIn(candidates).stream()
                    .collect(Collectors.toMap(UserRepository.UserNameView::getId,
                            UserRepository.UserNameView::getName));

            // 후보 4명의 카테고리 옵션 조회
            List<UserCategorySelectionRepository.UserOptionView> views =
                    userCategorySelectionRepository.findOptionLabelsByUsersAndCategory(candidates, cat.getId());

            // userId -> label 매핑
            Map<Long, String> optionLabelByUserId = views.stream()
                    .collect(Collectors.toMap(UserCategorySelectionRepository.UserOptionView::getUserId,
                            UserCategorySelectionRepository.UserOptionView::getLabel));

            List<String> optionLabelsInOrder = candidates.stream()
                    .map(uid -> optionLabelByUserId.get(uid))
                    .toList();

            String prompt = """
                역할: 당신은 짧고 자연스러운 한국어 칭찬 문구를 만드는 도우미입니다.
                카테고리: %s
                아래 4개 키워드를 모두 자연스럽게 반영해, 특정 인물 이름 없이 사용할 수 있는 한 문장을 만들어주세요.
                키워드: %s
                제약:
                - 1문장, 25자 내외
                - 과장/비속어 금지
                출력: 문장만
                """.formatted(cat.getCode(), String.join(", ", optionLabelsInOrder));

            String gptLine = chatClient.prompt(prompt).call().content();

            // 벡터검색
            SearchRequest req = SearchRequest.builder()
                    .query(gptLine == null ? "" : gptLine)
                    .topK(5)
                    .filterExpression("category == '" + cat.getCode().replace("'", "''") + "'")
                    .build();

            List<Document> docs = vectorStore.similaritySearch(req);
            if (docs.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 카테고리 템플릿 없음");

            // 랜덤 1개
            Document picked = docs.get(rnd.nextInt(docs.size()));
            Object tid = picked.getMetadata().get("templateId");
            if (tid == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "템플릿 ID 메타데이터 누락");
            Long templateId = Long.valueOf(tid.toString());

            ComplimentTemplate template = complimentTemplateRepository.findById(templateId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"템플릿을 찾을 수 없습니다."));

            // Compliment 저장
            Compliment draft = Compliment.builder()
                    .sender(sender)
                    .receiver(null)
                    .category(cat)
                    .template(template)
                    .score(null)
                    .build();
            Compliment saved = complimentRepository.save(draft);

            // 응답 DTO
            ComplimentGenerateResponse dto = new ComplimentGenerateResponse();
            dto.setClubId(clubId);
            dto.setSenderId(senderId);
            dto.setComplimentId(saved.getId());
            dto.setCategory(cat.getCode());
            dto.setText(template.getText());

            List<ComplimentGenerateResponse.CandidateInfo> candInfos = candidates.stream().map(uid -> {
                var ci = new ComplimentGenerateResponse.CandidateInfo();
                ci.setUserId(uid);
                ci.setUserName(nameById.get(uid));
                return ci;
            }).toList();
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
    }
}
