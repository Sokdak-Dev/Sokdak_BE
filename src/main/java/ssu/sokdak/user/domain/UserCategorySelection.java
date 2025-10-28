package ssu.sokdak.user.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import ssu.sokdak.category.domain.Category;
import ssu.sokdak.category.domain.CategoryOption;

@Entity
@Table(name = "user_category_selections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "option_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserCategorySelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 복합키보다 단일키가 관리 편함

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private CategoryOption option;

    private Integer rank;

    @Column(name = "selected_at")
    private LocalDateTime selectedAt;
}
