package ssu.sokdak.badge.api;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssu.sokdak.badge.domain.Badge;
import ssu.sokdak.badge.domain.UserBadge;
import ssu.sokdak.badge.dto.BadgeDtos;
import ssu.sokdak.badge.service.BadgeService;
import ssu.sokdak.user.api.MemberController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    // 뱃지 생성(운영용)
    @PostMapping("/badges")
    public ResponseEntity<BadgeDtos.BadgeRes> create(@Valid @RequestBody BadgeDtos.CreateReq req) {
        Badge b = badgeService.create(req);
        return ResponseEntity.ok(new BadgeDtos.BadgeRes(b.getId(), b.getCode(), b.getName(), b.getDescription(), b.getIconUrl()));
    }

    // 뱃지 목록
    @GetMapping("/badges")
    public ResponseEntity<List<BadgeDtos.BadgeRes>> list() {
        return ResponseEntity.ok(
                badgeService.list().stream()
                        .map(b -> new BadgeDtos.BadgeRes(b.getId(), b.getCode(), b.getName(), b.getDescription(), b.getIconUrl()))
                        .toList()
        );
    }

    // 특정 유저에게 뱃지 부여
    @PostMapping("/badges/{badgeId}/grant")
    public ResponseEntity<Long> grant(@PathVariable Long badgeId,
                                      @RequestParam Long userId,
                                      @RequestParam(required=false) String source) {
        UserBadge ub = badgeService.grant(userId, badgeId, source);
        return ResponseEntity.ok(ub.getId());
    }

    // (세션 로그인 기준) 내 뱃지
    @GetMapping("/badges/mine")
    public ResponseEntity<List<String>> myBadges(HttpSession session) {
        Long memberId = (Long) session.getAttribute(MemberController.SESSION_KEY);
        if (memberId == null) throw new IllegalStateException("로그인이 필요합니다.");
        return ResponseEntity.ok(
                badgeService.userBadges(memberId).stream().map(ub -> ub.getBadge().getCode()).toList()
        );
    }

    // 특정 유저의 뱃지
    @GetMapping("/members/{userId}/badges")
    public ResponseEntity<List<String>> userBadges(@PathVariable Long userId) {
        return ResponseEntity.ok(
                badgeService.userBadges(userId).stream().map(ub -> ub.getBadge().getCode()).toList()
        );
    }
}

