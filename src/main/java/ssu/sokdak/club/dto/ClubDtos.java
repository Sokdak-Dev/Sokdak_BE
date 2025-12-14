package ssu.sokdak.club.dto;

import java.time.LocalDateTime;
import java.util.List;
import ssu.sokdak.club.domain.Club;

public class ClubDtos {

    public record CreateClubRequest(String name, String description) { }

    public record CreateClubResponse(Long clubId) { }

    public record DeleteClubResponse(Long clubId, String result) { }

    public record JoinClubResponse(
            Long clubId,
            Long userId,
            String requestStatus
    ) { }

    public record ApproveClubMemberResponse(
            Long clubId,
            Long userId,
            String requestStatus
    ) { }

    public record RejectClubMemberResponse(
            Long clubId,
            Long userId,
            String requestStatus
    ) { }

    // [신규] 내 동아리 목록용 간단 DTO (MemberRes에 사용)
    public record ClubSimpleRes(Long id, String name, String university) {
        public static ClubSimpleRes from(Club club) {
            // university는 현재 DB에 없으므로 null 혹은 고정값 전달 (프론트 요청에 따라 수정 가능)
            return new ClubSimpleRes(club.getId(), club.getName(), null);
        }
    }

    // 멤버 리스트에 사진(avatarUrl)이 있어야 함
    public record ClubDetailMember(
            Long userId,
            String name,
            String avatarUrl
    ) { }

    public record ClubDetailResponse(
            Long clubId,
            String name,
            String description,
            int activeMemberCount,
            List<ClubDetailMember> activeMembers,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) { }

    public record ClubMembersResponse(
            Long clubId,
            boolean active,
            int count,
            List<ClubDetailMember> members,
            List<ClubDetailMember> rankings // [추가] 랭킹 섹션 에러 방지용
    ) { }

    // [추가] 검색 결과 DTO
    public record ClubSearchResponse(
            Long clubId,
            String name,
            String description,
            LocalDateTime createdAt,
            int activeMemberCount // [추가] 검색 카드 UI용
    ) {
        public static ClubSearchResponse from(Club club, int activeMemberCount) {
            return new ClubSearchResponse(
                    club.getId(),
                    club.getName(),
                    club.getDescription(),
                    club.getCreatedAt(),
                    activeMemberCount // 추가된 필드 설정
            );
        }
    }
}