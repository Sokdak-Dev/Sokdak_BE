package ssu.sokdak.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssu.sokdak.club.domain.ClubMember;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

    // ClubMember의 유니크제약 기반 중복 신청 방지 및 권한 확인에 사용
    boolean existsByClubIdAndUserId(Long clubId, Long userId);

    // 권한 확인에 사용
    Optional<ClubMember> findByClubIdAndUserId(Long clubId, Long userId);

    // 클럽 삭제 시 멤버 먼저 지운 다음 클럽 삭제
    @Modifying
    @Query("delete from ClubMember m where m.club.id = :clubId")
    void deleteAllByClubId(@Param("clubId") Long clubId);

    @Query("""
        select distinct cm.user.id
        from ClubMember cm
        where cm.club.id = :clubId
          and cm.active = true
          and cm.user.id <> :excludeUserId
    """)
    List<Long> findActiveMemberIdsExcluding(
            @Param("clubId") Long clubId,
            @Param("excludeUserId") Long excludeUserId);

}