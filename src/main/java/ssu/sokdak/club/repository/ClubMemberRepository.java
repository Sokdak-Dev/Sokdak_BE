package ssu.sokdak.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssu.sokdak.club.domain.ClubMember;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

    // 권한 및 중복 체크
    boolean existsByClubIdAndUserId(Long clubId, Long userId);
    Optional<ClubMember> findByClubIdAndUserId(Long clubId, Long userId);

    // [MemberService 연동용] 로그인 시 내 동아리 목록 조회
    List<ClubMember> findByUserIdAndActiveTrue(Long userId);

    // 삭제 로직
    @Modifying
    @Query("delete from ClubMember m where m.club.id = :clubId")
    void deleteAllByClubId(@Param("clubId") Long clubId);

    // [★추가] 멤버 목록 조회 시 User 정보와 Role을 한 번에 가져오는 쿼리 (성능 최적화 + Role 조회)
    @Query("select cm from ClubMember cm join fetch cm.user where cm.club.id = :clubId and cm.active = true")
    List<ClubMember> findWithUserByClubIdAndActiveTrue(@Param("clubId") Long clubId);

    // [★추가] 대기 멤버 목록 조회 (User 정보 포함)
    @Query("select cm from ClubMember cm join fetch cm.user where cm.club.id = :clubId and cm.active = false")
    List<ClubMember> findWithUserByClubIdAndActiveFalse(@Param("clubId") Long clubId);

    // 칭찬 대상 조회 (ID만 필요)
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

    // 단순 카운트용 (ID만 조회)
    @Query("select cm.user.id from ClubMember cm where cm.club.id = :clubId and cm.active = true")
    List<Long> findActiveMemberIdsByClubId(@Param("clubId") Long clubId);

    // 대기 멤버 ID 조회
    @Query("select cm.user.id from ClubMember cm where cm.club.id = :clubId and cm.active = false")
    List<Long> findPendingMemberIdsByClubId(@Param("clubId") Long clubId);
}