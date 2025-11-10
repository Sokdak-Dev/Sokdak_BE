package ssu.sokdak.club.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssu.sokdak.club.dto.ClubDtos.CreateClubRequest;
import ssu.sokdak.club.dto.ClubDtos.CreateClubResponse;
import ssu.sokdak.club.dto.ClubDtos.DeleteClubResponse;
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
}