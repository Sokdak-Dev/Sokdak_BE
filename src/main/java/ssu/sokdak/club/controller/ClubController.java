package ssu.sokdak.club.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ssu.sokdak.club.dto.ClubDtos.ApproveClubMemberResponse;
import ssu.sokdak.club.dto.ClubDtos.ClubDetailResponse;
import ssu.sokdak.club.dto.ClubDtos.ClubMembersResponse;
import ssu.sokdak.club.dto.ClubDtos.CreateClubRequest;
import ssu.sokdak.club.dto.ClubDtos.CreateClubResponse;
import ssu.sokdak.club.dto.ClubDtos.DeleteClubResponse;
import ssu.sokdak.club.dto.ClubDtos.JoinClubResponse;
import ssu.sokdak.club.dto.ClubDtos.RejectClubMemberResponse;
import ssu.sokdak.club.service.ClubService;

// 헤더에서 userId를 받고, 로그인 구현 후에 교체 필요
// 임시로 헤더: X-User-Id: <사용자ID>
@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    // 동아리 생성
    @PostMapping
    public ResponseEntity<CreateClubResponse> create(
            @RequestBody CreateClubRequest req,
            @RequestHeader("X-User-Id") Long userId
    ) {
        Long id = clubService.createClub(req, userId);
        return ResponseEntity.ok(new CreateClubResponse(id));
    }

    // 동아리 삭제
    @DeleteMapping("/{clubId}")
    public ResponseEntity<DeleteClubResponse> delete(
            @PathVariable Long clubId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        clubService.deleteClub(clubId, userId);
        return ResponseEntity.ok(new DeleteClubResponse(clubId, "deleted"));
    }

    // 동아리 상세 조회 (기본 정보 + 승인된 멤버 요약)
    @GetMapping("/{clubId}")
    public ResponseEntity<ClubDetailResponse> getClubDetail(
            @PathVariable Long clubId
    ) {
        ClubDetailResponse response = clubService.getClubDetail(clubId);
        return ResponseEntity.ok(response);
    }

    // 동아리 가입 신청
    @PostMapping("/{clubId}/join")
    public ResponseEntity<JoinClubResponse> join(
            @PathVariable Long clubId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        JoinClubResponse res = clubService.requestJoin(clubId, userId);
        return ResponseEntity.ok(res);
    }

    // 동아리 가입 승인
    @PostMapping("/{clubId}/members/{userId}/approve")
    public ResponseEntity<ApproveClubMemberResponse> approve(
            @PathVariable Long clubId,
            @PathVariable("userId") Long targetUserId,
            @RequestHeader("X-User-Id") Long managerUserId
    ) {
        ApproveClubMemberResponse res =
                clubService.approveJoinRequest(clubId, managerUserId, targetUserId);
        return ResponseEntity.ok(res);
    }

    // 동아리 가입 거절
    @PostMapping("/{clubId}/members/{userId}/reject")
    public ResponseEntity<RejectClubMemberResponse> reject(
            @PathVariable Long clubId,
            @PathVariable("userId") Long targetUserId,
            @RequestHeader("X-User-Id") Long managerUserId
    ) {
        RejectClubMemberResponse res =
                clubService.rejectJoinRequest(clubId, managerUserId, targetUserId);
        return ResponseEntity.ok(res);
    }

    // 멤버 목록 조회 -> 승인된 멤버 목록 (공개), 가입 대기 멤버 목록 (manager 전용)
    @GetMapping("/{clubId}/members")
    public ResponseEntity<ClubMembersResponse> getMembers(
            @PathVariable Long clubId,
            @RequestParam("active") boolean active,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        if (active) {
            // 승인된 멤버 목록은 공개 조회 가능 (userId 필요 없음)
            ClubMembersResponse res = clubService.getActiveMembers(clubId);
            return ResponseEntity.ok(res);
        }

        // 가입 대기 목록은 manager만 조회 가능 -> X-User-Id 필수
        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "가입 대기 멤버 목록 조회에는 X-User-Id 헤더가 필요합니다."
            );
        }

        ClubMembersResponse res = clubService.getPendingMembers(clubId, userId);
        return ResponseEntity.ok(res);
    }
}