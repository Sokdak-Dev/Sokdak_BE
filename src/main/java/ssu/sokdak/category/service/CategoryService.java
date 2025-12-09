package ssu.sokdak.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssu.sokdak.category.domain.Category;
import ssu.sokdak.category.domain.CategoryOption;
import ssu.sokdak.category.dto.CategoryDtos;
import ssu.sokdak.category.repository.CategoryOptionRepository;
import ssu.sokdak.category.repository.CategoryRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryOptionRepository categoryOptionRepository;

    public List<CategoryDtos.CategoryRes> findAllWithOptions() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            return Collections.emptyList();
        }

        List<CategoryOption> options = categoryOptionRepository.findByCategoryIn(categories);
        Map<Long, List<CategoryDtos.OptionRes>> optionsByCategoryId = options.stream()
                .collect(Collectors.groupingBy(opt -> opt.getCategory().getId(),
                        Collectors.mapping(CategoryDtos.OptionRes::of, Collectors.toList())));

        return categories.stream()
                .map(cat -> CategoryDtos.CategoryRes.of(cat,
                        optionsByCategoryId.getOrDefault(cat.getId(), Collections.emptyList())))
                .toList();
    }
}

