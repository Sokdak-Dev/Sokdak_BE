package ssu.sokdak.compliment.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import ssu.sokdak.category.domain.Category;

@Entity
@Table(name = "compliment_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ComplimentTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
