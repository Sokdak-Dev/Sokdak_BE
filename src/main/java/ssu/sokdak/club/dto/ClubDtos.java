package ssu.sokdak.club.dto;

public class ClubDtos {

    // 동아리 생성 요청: 우선 이름이랑 설명만 구현
    public record CreateClubRequest(String name, String description) { }

    // 생성된 clubId 반환
    public record CreateClubResponse(Long clubId) { }

    // 삭제된 clubId와 결과 문자열 ex) "~ 동아리가 삭제되었습니다."
    public record DeleteClubResponse(Long clubId, String result) { }

    // 동아리 가입 신청 결과 응답
    public record JoinClubResponse(
            Long clubId,
            Long userId,
            String requestStatus
    ) { }

    // 동아리 가입 승인 결과 응답
    public record ApproveClubMemberResponse(
            Long clubId,
            Long userId,
            String requestStatus
    ) { }

    // 동아리 가입 거절 결과 응답
    public record RejectClubMemberResponse(
            Long clubId,
            Long userId,
            String requestStatus
    ) { }
}