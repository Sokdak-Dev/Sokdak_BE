package ssu.sokdak.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssu.sokdak.club.domain.Club;
import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long> {
    // 동아리 이름 중복 방지
    boolean existsByName(String name);

    // 이름 또는 설명에 키워드가 포함된 동아리 검색
    List<Club> findByNameContainingOrDescriptionContaining(String name, String description);
}