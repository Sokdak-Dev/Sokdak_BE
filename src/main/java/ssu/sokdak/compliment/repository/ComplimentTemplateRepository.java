package ssu.sokdak.compliment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssu.sokdak.compliment.domain.ComplimentTemplate;

import java.util.Optional;

public interface ComplimentTemplateRepository extends JpaRepository<ComplimentTemplate, Long> {
    Optional<ComplimentTemplate> findById(Long id);
}
