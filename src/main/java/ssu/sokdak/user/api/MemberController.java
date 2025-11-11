package ssu.sokdak.user.api;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssu.sokdak.user.domain.User;
import ssu.sokdak.user.dto.MemberDtos;
import ssu.sokdak.user.service.MemberService;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    public static final String SESSION_KEY = "LOGIN_MEMBER_ID";
    private final MemberService memberService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<MemberDtos.MemberRes> register(@Valid @RequestBody MemberDtos.RegisterReq req) {
        User u = memberService.register(req);
        return ResponseEntity.ok(MemberDtos.MemberRes.from(u));
    }

    // 로그인 (세션)
    @PostMapping("/login")
    public ResponseEntity<MemberDtos.MemberRes> login(@Valid @RequestBody MemberDtos.LoginReq req,
                                                      HttpSession session) {
        User u = memberService.login(req);
        session.setAttribute(SESSION_KEY, u.getId());
        return ResponseEntity.ok(MemberDtos.MemberRes.from(u));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    // 회원 정보 조회 (id로)
    @GetMapping
    public ResponseEntity<MemberDtos.MemberRes> get(@RequestParam Long id) {
        return ResponseEntity.ok(MemberDtos.MemberRes.from(memberService.get(id)));
    }

    // 회원 정보 수정
    @PatchMapping
    public ResponseEntity<MemberDtos.MemberRes> update(@RequestParam Long id,
                                                       @RequestBody MemberDtos.UpdateReq req) {
        return ResponseEntity.ok(MemberDtos.MemberRes.from(memberService.update(id, req)));
    }

    // 회원 탈퇴(비활성화)
    @DeleteMapping
    public ResponseEntity<Void> deactivate(@RequestParam Long id) {
        memberService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    // 내 정보 조회 (세션)
    @GetMapping("/me")
    public ResponseEntity<MemberDtos.MemberRes> me(HttpSession session) {
        Long memberId = (Long) session.getAttribute(SESSION_KEY);
        if (memberId == null) throw new IllegalStateException("로그인이 필요합니다.");
        return ResponseEntity.ok(MemberDtos.MemberRes.from(memberService.get(memberId)));
    }
}

