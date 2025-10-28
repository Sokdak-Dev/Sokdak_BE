package ssu.sokdak.compliment.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "template_embeddings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TemplateEmbedding {

    @Id
    @Column(name = "template_id")
    private Long templateId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "template_id")
    private ComplimentTemplate template;

    private String model;
    private Integer dim;

    @Column(name = "vector_ref")
    private String vectorRef;

    @Column(columnDefinition = "json")
    private String meta = "{}";
}
