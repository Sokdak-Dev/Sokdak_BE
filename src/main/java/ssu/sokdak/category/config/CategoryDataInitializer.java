package ssu.sokdak.category.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ssu.sokdak.category.domain.Category;
import ssu.sokdak.category.domain.CategoryOption;
import ssu.sokdak.category.repository.CategoryOptionRepository;
import ssu.sokdak.category.repository.CategoryRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final CategoryOptionRepository categoryOptionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, List<String>> defaults = new LinkedHashMap<>();
        defaults.put("LEADERSHIP", List.of("비전형 리더", "섬세한 조율자", "카리스마형", "솔선수범형", "믿음직한 후원자"));
        defaults.put("CREATIVITY", List.of("아이디어 폭포", "실험정신 충만", "문제 해결사", "트렌드 세터", "관점 전환가"));
        defaults.put("TEAMWORK", List.of("분위기 메이커", "갈등 조정자", "든든한 서포터", "의사소통 챔피언", "배려왕"));
        defaults.put("PROFESSIONALISM", List.of("디테일 장인", "마감의 신", "데이터 기반형", "표준 수호자", "품질 집착러"));
        defaults.put("GROWTH", List.of("성장 모험가", "피드백 러버", "학습 전도사", "도전 설계자", "꾸준함의 아이콘"));

        defaults.forEach(this::ensureCategoryWithOptions);
    }

    private void ensureCategoryWithOptions(String code, List<String> options) {

        // code -> nameKo 매핑
        String nameKo = switch (code) {
            case "LEADERSHIP" -> "리더십";
            case "CREATIVITY" -> "창의성";
            case "TEAMWORK" -> "팀워크";
            case "PROFESSIONALISM" -> "프로페셔널리즘";
            case "GROWTH" -> "성장";
            default -> code;
        };

        Category category = categoryRepository.findByCode(code)
                .orElseGet(() -> categoryRepository.save(
                        Category.builder()
                                .code(code)
                                .nameKo(nameKo)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                ));

        options.forEach(label ->
                categoryOptionRepository.findByCategoryAndLabel(category, label)
                        .orElseGet(() -> categoryOptionRepository.save(
                                CategoryOption.builder()
                                        .category(category)
                                        .label(label)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build()
                        ))
        );
    }

}

