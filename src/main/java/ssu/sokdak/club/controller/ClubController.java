package ssu.sokdak.club.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ssu.sokdak.club.dto.ClubDtos.*;
import ssu.sokdak.club.service.ClubService;
import ssu.sokdak.user.api.MemberController;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    // 세션에서 로그인된 사용자 ID 추출
    private Long getLoginUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute(MemberController.SESSION_KEY);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return userId;
    }

    // 동아리 생성
    @PostMapping
    public ResponseEntity<CreateClubResponse> create(
            @RequestBody CreateClubRequest req,
            HttpSession session
    ) {
        Long userId = getLoginUserId(session);
        Long id = clubService.createClub(req, userId);
        return ResponseEntity.ok(new CreateClubResponse(id));
    }

    // 동아리 삭제
    @DeleteMapping("/{clubId}")
    public ResponseEntity<DeleteClubResponse> delete(
            @PathVariable Long clubId,
            HttpSession session
    ) {
        Long userId = getLoginUserId(session);
        clubService.deleteClub(clubId, userId);
        return ResponseEntity.ok(new DeleteClubResponse(clubId, "deleted"));
    }

    // 동아리 검색 (로그인 불필요)
    @GetMapping("/search")
    public ResponseEntity<List<ClubSearchResponse>> search(@RequestParam("q") String query) {
        List<ClubSearchResponse> result = clubService.searchClubs(query);
        return ResponseEntity.ok(result);
    }

    // 동아리 상세 조회
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
            HttpSession session
    ) {
        Long userId = getLoginUserId(session);
        JoinClubResponse res = clubService.requestJoin(clubId, userId);
        return ResponseEntity.ok(res);
    }

    // 동아리 가입 승인
    @PostMapping("/{clubId}/members/{userId}/approve")
    public ResponseEntity<ApproveClubMemberResponse> approve(
            @PathVariable Long clubId,
            @PathVariable("userId") Long targetUserId,
            HttpSession session
    ) {
        Long managerUserId = getLoginUserId(session);
        ApproveClubMemberResponse res = clubService.approveJoinRequest(clubId, managerUserId, targetUserId);
        return ResponseEntity.ok(res);
    }

    // 동아리 가입 거절
    @PostMapping("/{clubId}/members/{userId}/reject")
    public ResponseEntity<RejectClubMemberResponse> reject(
            @PathVariable Long clubId,
            @PathVariable("userId") Long targetUserId,
            HttpSession session
    ) {
        Long managerUserId = getLoginUserId(session);
        RejectClubMemberResponse res = clubService.rejectJoinRequest(clubId, managerUserId, targetUserId);
        return ResponseEntity.ok(res);
    }

    // 멤버 목록 조회
    @GetMapping("/{clubId}/members")
    public ResponseEntity<ClubMembersResponse> getMembers(
            @PathVariable Long clubId,
            @RequestParam("active") boolean active,
            HttpSession session
    ) {
        if (active) {
            // 승인된 멤버 목록 조회 (공개)
            return ResponseEntity.ok(clubService.getActiveMembers(clubId));
        }

        // 가입 대기 목록은 매니저만 조회 가능 -> 로그인 필수
        Long userId = getLoginUserId(session);
        return ResponseEntity.ok(clubService.getPendingMembers(clubId, userId));
    }
}