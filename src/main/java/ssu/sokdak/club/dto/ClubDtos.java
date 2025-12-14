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

    // [신규] 내 동아리 목록용 (MemberRes에서 사용)
    public record ClubSimpleRes(Long id, String name, String university) {
        public static ClubSimpleRes from(Club club) {
            return new ClubSimpleRes(club.getId(), club.getName(), null);
        }
    }

    // [수정] role 필드 추가 (동아리장 구분용)
    public record ClubDetailMember(
            Long userId,
            String name,
            String avatarUrl,
            String role // "manager" or "member"
    ) {
        public static ClubDetailMember of(Long userId, String name, String avatarUrl, String role) {
            return new ClubDetailMember(userId, name, avatarUrl, role);
        }
    }

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
            List<ClubDetailMember> rankings
    ) { }

    public record ClubSearchResponse(
            Long clubId,
            String name,
            String description,
            LocalDateTime createdAt,
            int activeMemberCount
    ) {
        public static ClubSearchResponse from(Club club, int activeMemberCount) {
            return new ClubSearchResponse(
                    club.getId(),
                    club.getName(),
                    club.getDescription(),
                    club.getCreatedAt(),
                    activeMemberCount
            );
        }
    }
}