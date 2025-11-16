package ssu.sokdak.club.dto;

import java.time.LocalDateTime;
import java.util.List;

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

    // 동아리 상세 조회 응답에서 사용하는 멤버 요약 정보 -> 명단 사용할 때 용도
    public record ClubDetailMember(
            Long userId,
            String name
    ) { }

    // 동아리 상세 조회 응답 -> 승인된 멤버수 + 리스트 포함
    public record ClubDetailResponse(
            Long clubId,
            String name,
            String description,
            int activeMemberCount,
            List<ClubDetailMember> activeMembers,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) { }
}