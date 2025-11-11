package ssu.sokdak.compliment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ssu.sokdak.compliment.domain.Compliment;

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
}
