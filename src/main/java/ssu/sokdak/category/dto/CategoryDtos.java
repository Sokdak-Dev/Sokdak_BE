package ssu.sokdak.category.dto;

import ssu.sokdak.category.domain.Category;
import ssu.sokdak.category.domain.CategoryOption;

import java.util.List;

public class CategoryDtos {

    public record CategoryRes(Long id, String code, List<OptionRes> options) {
        public static CategoryRes of(Category category, List<OptionRes> options) {
            return new CategoryRes(category.getId(), category.getCode(), options);
        }
    }

    public record OptionRes(Long id, String label) {
        public static OptionRes of(CategoryOption option) {
            return new OptionRes(option.getId(), option.getLabel());
        }
    }
}

