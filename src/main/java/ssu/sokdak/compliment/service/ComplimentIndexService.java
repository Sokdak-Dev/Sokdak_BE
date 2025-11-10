package ssu.sokdak.compliment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import ssu.sokdak.compliment.domain.ComplimentTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComplimentIndexService {

    private final VectorStore vectorStore;

    public void indexCompliment(ComplimentTemplate complimentTemplate) {

        String embeddingText = complimentTemplate.getText();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", complimentTemplate.getCategory().getCode());
        metadata.put("templateId", complimentTemplate.getId());

        Document doc = Document.builder()
                .id(UUID.randomUUID().toString())
                .text(embeddingText)
                .metadata(metadata)
                .build();

        vectorStore.add(List.of(doc));
    }
}

