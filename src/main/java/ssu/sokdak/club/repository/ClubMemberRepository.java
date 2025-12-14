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

    // [★필수 추가★] MemberService에서 로그인 시 내 동아리 목록을 가져오기 위해 이게 꼭 있어야 합니다!
    List<ClubMember> findByUserIdAndActiveTrue(Long userId);

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
            @Param("excludeUserId") Long excludeUserId
    );

    // 동아리 내 "승인된" 멤버들의 userId 목록 조회 -> 승인된 멤버수 + 이름 목록용
    @Query("""
        select cm.user.id
        from ClubMember cm
        where cm.club.id = :clubId
          and cm.active = true
    """)
    List<Long> findActiveMemberIdsByClubId(@Param("clubId") Long clubId);

    // 동아리 내 "가입 대기 중" 멤버들의 userId 목록 조회 -> 대기 멤버 목록용
    @Query("""
        select cm.user.id
        from ClubMember cm
        where cm.club.id = :clubId
          and cm.active = false
    """)
    List<Long> findPendingMemberIdsByClubId(@Param("clubId") Long clubId);
}