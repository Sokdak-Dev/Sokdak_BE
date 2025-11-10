package ssu.sokdak.compliment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssu.sokdak.compliment.domain.Compliment;

public interface ComplimentRepository extends JpaRepository<Compliment, Long> {
}
