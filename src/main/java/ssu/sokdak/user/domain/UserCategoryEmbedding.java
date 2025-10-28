package ssu.sokdak.user.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import ssu.sokdak.category.domain.Category;

@Entity
@Table(name = "user_category_embeddings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserCategoryEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private String model;
    private Integer dim;

    @Column(name = "vector_ref")
    private String vectorRef;

    @Column(columnDefinition = "json")
    private String meta = "{}";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
