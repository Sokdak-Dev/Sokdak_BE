package ssu.sokdak.compliment.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import ssu.sokdak.user.domain.User;
import ssu.sokdak.club.domain.Club;

@Entity
@Table(name = "compliment_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ComplimentStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    private Integer sentCount = 0;
    private Integer receivedCount = 0;

    private LocalDateTime lastSentAt;
    private LocalDateTime lastReceivedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
