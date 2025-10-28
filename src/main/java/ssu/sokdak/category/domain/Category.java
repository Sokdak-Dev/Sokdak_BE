package ssu.sokdak.category.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // e.g. LEADERSHIP

    @Column(name = "name_ko", nullable = false)
    private String nameKo;

    @Column(name = "name_en")
    private String nameEn;

    private String description;
}
