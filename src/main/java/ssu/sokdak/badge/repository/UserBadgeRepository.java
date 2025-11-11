package ssu.sokdak.badge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssu.sokdak.badge.domain.UserBadge;
import ssu.sokdak.user.domain.User;
import ssu.sokdak.badge.domain.Badge;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    boolean existsByUserAndBadge(User user, Badge badge);
    List<UserBadge> findByUser(User user);
}

