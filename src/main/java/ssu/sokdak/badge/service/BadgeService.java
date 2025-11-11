package ssu.sokdak.badge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssu.sokdak.badge.BadgeCodes;
import ssu.sokdak.badge.domain.Badge;
import ssu.sokdak.badge.domain.UserBadge;
import ssu.sokdak.badge.dto.BadgeDtos;
import ssu.sokdak.badge.repository.BadgeRepository;
import ssu.sokdak.badge.repository.UserBadgeRepository;
import ssu.sokdak.compliment.repository.ComplimentRepository;
import ssu.sokdak.user.domain.User;
import ssu.sokdak.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;

    // 누적/스트릭 계산용
    private final ComplimentRepository complimentRepository;

    /* ---------------------- 기존 기능 그대로 ---------------------- */

    @Transactional
    public Badge create(BadgeDtos.CreateReq req) {
        if (badgeRepository.existsByCode(req.code()))
            throw new IllegalArgumentException("이미 존재하는 뱃지 코드입니다.");
        Badge b = Badge.builder()
                .code(req.code())
                .name(req.name())
                .description(req.description())
                .iconUrl(req.iconUrl())
                .condition(req.conditionJson() != null ? req.conditionJson() : "{}")
                .createdAt(LocalDateTime.now())
                .build();
        return badgeRepository.save(b);
    }

    public List<Badge> list() { return badgeRepository.findAll(); }

    @Transactional
    public UserBadge grant(Long userId, Long badgeId, String source) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("뱃지 없음"));

        if (userBadgeRepository.existsByUserAndBadge(user, badge))
            throw new IllegalArgumentException("이미 보유한 뱃지입니다.");

        UserBadge ub = UserBadge.builder()
                .user(user)
                .badge(badge)
                .earnedAt(LocalDateTime.now())
                .source(source)
                .build();
        return userBadgeRepository.save(ub);
    }

    public List<UserBadge> userBadges(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        return userBadgeRepository.findByUser(u);
    }

    /* ---------------------- 추가: 멱등 수여 헬퍼 ---------------------- */

    @Transactional
    public boolean grantIfNotOwnedByCode(Long userId, String badgeCode, String source) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Badge badge = badgeRepository.findByCode(badgeCode)
                .orElseThrow(() -> new IllegalArgumentException("뱃지 코드가 유효하지 않습니다."));

        if (userBadgeRepository.existsByUserAndBadge(user, badge)) {
            return false;
        }
        UserBadge ub = UserBadge.builder()
                .user(user)
                .badge(badge)
                .earnedAt(LocalDateTime.now())
                .source(source)
                .build();
        userBadgeRepository.save(ub);
        return true;
    }

    /* --------------- 추가: 칭찬 1건 생성 시 즉시 평가/발급 --------------- */

    /**
     * 칭찬 저장 직후 “한 줄 호출”용.
     * - 첫 칭찬
     * - 누적(보낸/받은) 10/50/100
     * - 스트릭(3/7일) — Asia/Seoul 기준
     */
    @Transactional
    public void evaluateAndGrantOnCompliment(Long senderId, Long receiverId, LocalDateTime createdAtKst) {
        // A) 누적/마일스톤
        long totalSent = complimentRepository.countBySender_Id(senderId);
        long totalRecv = complimentRepository.countByReceiver_Id(receiverId);

        if (totalSent == 1) {
            grantIfNotOwnedByCode(senderId, BadgeCodes.FIRST_COMPLIMENT, "첫 칭찬");
        }
        if (totalSent >= 100) {
            grantIfNotOwnedByCode(senderId, BadgeCodes.TOP_SENDER_100, "누적 보낸 100");
        } else if (totalSent >= 50) {
            grantIfNotOwnedByCode(senderId, BadgeCodes.TOP_SENDER_50, "누적 보낸 50");
        } else if (totalSent >= 10) {
            grantIfNotOwnedByCode(senderId, BadgeCodes.TOP_SENDER_10, "누적 보낸 10");
        }

        if (totalRecv >= 100) {
            grantIfNotOwnedByCode(receiverId, BadgeCodes.TOP_RECEIVER_100, "누적 받은 100");
        } else if (totalRecv >= 50) {
            grantIfNotOwnedByCode(receiverId, BadgeCodes.TOP_RECEIVER_50, "누적 받은 50");
        } else if (totalRecv >= 10) {
            grantIfNotOwnedByCode(receiverId, BadgeCodes.TOP_RECEIVER_10, "누적 받은 10");
        }

        // B) 스트릭(3/7일) — Asia/Seoul 기준
        ZoneId KST = ZoneId.of("Asia/Seoul");
        LocalDate base = createdAtKst.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(KST)
                .toLocalDate();

        LocalDateTime from = base.minusDays(6).atStartOfDay(KST).toLocalDateTime(); // 최근 7일 [base-6 .. base]
        LocalDateTime to   = base.plusDays(1).atStartOfDay(KST).toLocalDateTime();

        List<LocalDate> days = complimentRepository
                .distinctSentDates(senderId, from, to)
                .stream().map(java.sql.Date::toLocalDate).toList();

        if (isStreak(days, base, 3)) {
            grantIfNotOwnedByCode(senderId, BadgeCodes.STREAK_3DAYS, "3일 연속 칭찬");
        }
        if (isStreak(days, base, 7)) {
            grantIfNotOwnedByCode(senderId, BadgeCodes.STREAK_7DAYS, "7일 연속 칭찬");
        }
    }

    private boolean isStreak(List<LocalDate> days, LocalDate base, int len) {
        for (int i = 0; i < len; i++) {
            if (!days.contains(base.minusDays(i))) return false;
        }
        return true;
    }
}
