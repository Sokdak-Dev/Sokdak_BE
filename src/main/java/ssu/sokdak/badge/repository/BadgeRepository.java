package ssu.sokdak.badge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import ssu.sokdak.badge.domain.Badge;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    boolean existsByCode(String code);
    Optional<Badge> findByCode(String code);
}
