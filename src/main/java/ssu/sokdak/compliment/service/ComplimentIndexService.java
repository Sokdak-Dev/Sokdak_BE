package ssu.sokdak.compliment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssu.sokdak.compliment.domain.ComplimentTemplate;
import ssu.sokdak.compliment.repository.ComplimentRepository;
import ssu.sokdak.compliment.repository.ComplimentTemplateRepository;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplimentIndexService {

    private final VectorStore vectorStore;
    private final ComplimentTemplateRepository complimentTemplateRepository;

    private String stableId(Long complimentTemplateId) {
        return UUID.nameUUIDFromBytes(
                ("compliment-" + complimentTemplateId).getBytes(StandardCharsets.UTF_8)
        ).toString();
    }

    public void indexCompliment(ComplimentTemplate complimentTemplate) {

        String embeddingText = complimentTemplate.getText();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", complimentTemplate.getCategory().getCode());
        metadata.put("templateId", complimentTemplate.getId());

        Document doc = Document.builder()
                .id(stableId(complimentTemplate.getId()))
                .text(embeddingText)
                .metadata(metadata)
                .build();

        vectorStore.add(List.of(doc));
    }

    @Transactional(readOnly = true)
    public int reindexAll(int batchSize) {
        List<ComplimentTemplate> templates = complimentTemplateRepository.findAll();
        if (templates.isEmpty()) return 0;

        List<Document> docs = templates.stream()
                .map(this::toDocument)
                .toList();

        int total = docs.size();
        for (int from = 0; from < total; from += batchSize) {
            int to = Math.min(from + batchSize, total);
            List<Document> chunk = docs.subList(from, to);

            long t0 = System.currentTimeMillis();
            vectorStore.add(chunk);
            log.info("[Reindex] added {} docs ({}~{}) in {}ms",
                    chunk.size(), from, to - 1, System.currentTimeMillis() - t0);
        }

        log.info("[Reindex] done total={}", total);
        return total;
    }

    private Document toDocument(ComplimentTemplate complimentTemplate) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", complimentTemplate.getCategory().getCode());
        metadata.put("templateId", complimentTemplate.getId());

        return Document.builder()
                .id(stableId(complimentTemplate.getId()))
                .text(complimentTemplate.getText())
                .metadata(metadata)
                .build();
    }
}

