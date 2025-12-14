package ssu.sokdak.compliment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ComplimentHistoryResponse {
    private Long complimentId;
    private Long userId;
    private String name;
    private String message;
    private Boolean anonymity;
    private LocalDateTime createdAt;
}