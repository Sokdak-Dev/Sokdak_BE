package ssu.sokdak.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssu.sokdak.category.domain.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCode(String code);
}
