package ssu.sokdak.category.dto;

import lombok.Builder;
import lombok.Getter;
import ssu.sokdak.category.domain.Category;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class CategoryContext {
    private final Category category;
    private final List<Long> candidateIds;
    private final Map<Long, String> nameById;
    private final List<String> optionLabelsInOrder;
}

