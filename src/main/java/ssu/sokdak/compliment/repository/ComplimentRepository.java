package ssu.sokdak.compliment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssu.sokdak.compliment.domain.Compliment;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface ComplimentRepository extends JpaRepository<Compliment, Long> {

    // 연관필드 경로 주의: sender는 User 연관이라 sender.id로 타고 들어가야 함
    long countBySender_Id(Long senderId);
    long countByReceiver_Id(Long receiverId);

    // 스트릭(최근 N일 보낸 날짜 집합) — MySQL 8 기준
    @Query(value = """
        SELECT DATE(c.created_at) AS d
        FROM compliments c
        WHERE c.sender_id = :userId
          AND c.created_at >= :from
          AND c.created_at <  :to
        GROUP BY DATE(c.created_at)
        """, nativeQuery = true)
    List<java.sql.Date> distinctSentDates(Long userId, LocalDateTime from, LocalDateTime to);

    @Query("""
        select c
        from Compliment c
        join fetch c.receiver r
        where c.sender.id = :userId
        order by c.createdAt desc
    """)
    List<Compliment> findSentByUserId(@Param("userId") Long userId);

    @Query("""
        select c
        from Compliment c
        join fetch c.sender s
        where c.receiver.id = :userId
        order by c.createdAt desc
    """)
    List<Compliment> findReceivedByUserId(@Param("userId") Long userId);

    //공용 프로젝션: userId + 받은 칭찬 수
    public interface ReceivedRankingProjection {
        Long getUserId();
        long getReceivedCount();
    }


    // 4.1.1 앱 전체 랭킹 (누적 기준)
    @Query("""
        select c.receiver.id as userId,
               count(c)        as receivedCount
        from Compliment c
        group by c.receiver.id
        order by count(c) desc
    """)
    List<ReceivedRankingProjection> findGlobalReceivedRanking(Pageable pageable);

    // 4.1.2 동아리 내 랭킹
    //  - ClubMember 엔티티에 active(boolean) 필드 있다고 가정
    //  - 필드명이 다르면 cm.active 부분만 네 필드명으로 바꿔줘
    @Query("""
        select cm.user.id as userId,
               count(c)     as receivedCount
        from ClubMember cm
        join Compliment c
          on c.receiver = cm.user
        where cm.club.id = :clubId
        group by cm.user.id
        order by count(c) desc
    """)
    List<ReceivedRankingProjection> findClubReceivedRanking(
            @Param("clubId") Long clubId,
            Pageable pageable
    );

    // 많이 보낸 사람 랭킹용 프로젝션
    public interface SentRankingProjection {
        Long getUserId();
        long getSentCount();
    }

    // 전체에서 많이 보낸 사람 TOP N
    @Query("""
        select c.sender.id as userId,
               count(c)       as sentCount
        from Compliment c
        group by c.sender.id
        order by count(c) desc
    """)
    List<SentRankingProjection> findGlobalSentRanking(Pageable pageable);

    // 동아리별로 많이 보낸 사람 TOP N
    @Query("""
        select cm.user.id as userId,
               count(c)     as sentCount
        from ClubMember cm
        join Compliment c
          on c.sender = cm.user
        where cm.club.id = :clubId
        group by cm.user.id
        order by count(c) desc
    """)
    List<SentRankingProjection> findClubSentRanking(
            @Param("clubId") Long clubId,
            Pageable pageable
    );

}
