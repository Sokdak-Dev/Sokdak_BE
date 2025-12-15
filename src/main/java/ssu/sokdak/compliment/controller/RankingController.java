package ssu.sokdak.compliment.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssu.sokdak.compliment.dto.RankingDtos;
import ssu.sokdak.compliment.service.RankingService;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * 4.1.1 앱 전체 랭킹
     * 예: GET /api/rankings/global?limit=10
     */
    @GetMapping("/global")
    public ResponseEntity<List<RankingDtos.GlobalRankRes>> globalRanking(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(rankingService.getGlobalRanking(limit));
    }

    /**
     * 4.1.2 동아리 내 랭킹
     * 예: GET /api/rankings/clubs/1?limit=10
     */
    @GetMapping("/clubs/{clubId}")
    public ResponseEntity<List<RankingDtos.ClubRankRes>> clubRanking(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(rankingService.getClubRanking(clubId, limit));
    }


    // 전체에서 많이 보낸 사람
    // GET /api/rankings/global/sent?limit=10

    @GetMapping("/global/sent")
    public ResponseEntity<List<RankingDtos.GlobalSentRankRes>> globalSentRanking(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(rankingService.getGlobalSentRanking(limit));
    }


    //  동아리별로 많이 보낸 사람
    // GET /api/rankings/clubs/{clubId}/sent?limit=10

    @GetMapping("/clubs/{clubId}/sent")
    public ResponseEntity<List<RankingDtos.ClubSentRankRes>> clubSentRanking(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(rankingService.getClubSentRanking(clubId, limit));
    }

    // 4.1.3 동아리별로 칭찬 많이 보낸 동아리 TOP N
    @GetMapping("/clubs/sent")
    public ResponseEntity<List<RankingDtos.ClubRankRes>> clubSentRanking(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<RankingDtos.ClubRankRes> clubRankings = rankingService.getClubRankingBySentCount(limit);
        return ResponseEntity.ok(clubRankings);
    }
}
