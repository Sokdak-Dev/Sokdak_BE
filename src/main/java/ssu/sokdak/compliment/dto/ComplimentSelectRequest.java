package ssu.sokdak.compliment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComplimentSelectRequest {
    private Long complimentId;
    private Long userId;
}
