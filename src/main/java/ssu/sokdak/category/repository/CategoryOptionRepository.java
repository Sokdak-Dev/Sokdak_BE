package ssu.sokdak.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssu.sokdak.category.domain.Category;
import ssu.sokdak.category.domain.CategoryOption;

import java.util.List;
import java.util.Optional;

public interface CategoryOptionRepository extends JpaRepository<CategoryOption, Long> {
    Optional<CategoryOption> findByCategoryAndLabel(Category category, String label);
    List<CategoryOption> findByCategoryIn(List<Category> categories);
}

