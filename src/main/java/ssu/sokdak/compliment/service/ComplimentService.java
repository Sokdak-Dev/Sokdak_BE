package ssu.sokdak.compliment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ssu.sokdak.category.domain.Category;
import ssu.sokdak.category.repository.CategoryRepository;
import ssu.sokdak.compliment.domain.ComplimentTemplate;
import ssu.sokdak.compliment.repository.ComplimentTemplateRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ComplimentService {

    private final ComplimentTemplateRepository complimentTemplateRepository;
    private final CategoryRepository categoryRepository;
    private final ComplimentIndexService complimentIndexService;

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
}
