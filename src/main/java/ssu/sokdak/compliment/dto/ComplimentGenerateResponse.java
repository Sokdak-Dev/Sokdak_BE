package ssu.sokdak.compliment.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ComplimentGenerateResponse {
    private Long clubId;
    private Long senderId;
    private Long complimentId;
    private String category;
    private String text;
    private List<CandidateInfo> candidates;

    @Getter @Setter
    public static class CandidateInfo {
        private Long userId;
        private String userName;
    }
}
