package ssu.sokdak.club.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import ssu.sokdak.user.domain.User;

@Entity
@Table(name = "club_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"club_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ClubMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private ssu.sokdak.club.domain.Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String role = "member";

    private Boolean active = true;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
    }

    public boolean isManager() {
        return "manager".equals(this.role);
    }

    // 가입 승인 처리: active=true + joinedAt
    public void approve(LocalDateTime approvedAt) {
        this.active = true;
        this.joinedAt = approvedAt;
    }
}