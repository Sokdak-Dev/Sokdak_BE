package ssu.sokdak.compliment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssu.sokdak.compliment.domain.ComplimentTemplate;

public interface ComplimentTemplateRepository extends JpaRepository<ComplimentTemplate, Long> {
}
