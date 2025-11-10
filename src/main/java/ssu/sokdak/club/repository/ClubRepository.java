package ssu.sokdak.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssu.sokdak.club.domain.Club;

public interface ClubRepository extends JpaRepository<Club, Long> {
    // 동아리 이름 중복 방지
    boolean existsByName(String name);
}