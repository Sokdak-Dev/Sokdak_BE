package ssu.sokdak.badge.dto;

import jakarta.validation.constraints.NotBlank;

public class BadgeDtos {

    public record CreateReq(
            @NotBlank String code,
            @NotBlank String name,
            String description,
            String iconUrl,
            String conditionJson // {"sent_count":10} 등
    ) {}

    public record BadgeRes(Long id, String code, String name, String description, String iconUrl) {}

    public record GrantReq(Long userId, Long badgeId, String source) {}
}

