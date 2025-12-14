package ssu.sokdak.compliment.dto;

public class RankingDtos {

    // 4.1.1 앱 전체 랭킹
    public record GlobalRankRes(
            int rank,          // 순위
            Long userId,
            String name,
            String avatarUrl,
            long receivedCount // 받은 칭찬 수
    ) {}

    // 4.1.2 동아리 내 랭킹
    public record ClubRankRes(
            int rank,
            Long userId,
            String name,
            String avatarUrl,
            long receivedCount
    ) {}
    // 앱 전체: 많이 보낸 랭킹
    public record GlobalSentRankRes(
            int rank,
            Long userId,
            String name,
            String avatarUrl,
            long sentCount
    ) {}
    // 동아리별: 많이 보낸 랭킹
    public record ClubSentRankRes(
            int rank,
            Long userId,
            String name,
            String avatarUrl,
            long sentCount
    ) {}
}
