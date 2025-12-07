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

    // 멤버 리스트에 사진(avatarUrl)이 있어야 함
    public record ClubDetailMember(
            Long userId,
            String name,
            String avatarUrl // 추가됨
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
            List<ClubDetailMember> members
    ) { }

    // [추가] 검색 결과 DTO
    public record ClubSearchResponse(
            Long clubId,
            String name,
            String description,
            LocalDateTime createdAt
    ) {
        public static ClubSearchResponse from(Club club) {
            return new ClubSearchResponse(
                    club.getId(),
                    club.getName(),
                    club.getDescription(),
                    club.getCreatedAt()
            );
        }
    }
}