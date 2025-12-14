package ssu.sokdak.compliment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssu.sokdak.compliment.dto.RankingDtos;
import ssu.sokdak.compliment.repository.ComplimentRepository;
import ssu.sokdak.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

    private final ComplimentRepository complimentRepository;
    private final UserRepository userRepository;

    /**
     * 4.1.1 앱 전체 랭킹
     * @param limit TOP N 개수
     */
    public List<RankingDtos.GlobalRankRes> getGlobalRanking(int limit) {
        var projections = complimentRepository.findGlobalReceivedRanking(
                PageRequest.of(0, limit)
        );

        // userId -> UserNameView 맵핑 (이름 + 프로필 이미지)
        List<Long> userIds = projections.stream()
                .map(ComplimentRepository.ReceivedRankingProjection::getUserId)
                .toList();

        Map<Long, UserRepository.UserNameView> userMap = userRepository.findByIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(UserRepository.UserNameView::getId, v -> v));

        AtomicInteger rankCounter = new AtomicInteger(1);

        return projections.stream()
                .map(p -> {
                    var view = userMap.get(p.getUserId());
                    String name = view != null ? view.getName() : null;
                    String avatarUrl = view != null ? view.getAvatarUrl() : null;

                    return new RankingDtos.GlobalRankRes(
                            rankCounter.getAndIncrement(),
                            p.getUserId(),
                            name,
                            avatarUrl,
                            p.getReceivedCount()
                    );
                })
                .toList();
    }

    /**
     * 4.1.2 동아리 내 랭킹
     */
    public List<RankingDtos.ClubRankRes> getClubRanking(Long clubId, int limit) {
        var projections = complimentRepository.findClubReceivedRanking(
                clubId,
                PageRequest.of(0, limit)
        );

        List<Long> userIds = projections.stream()
                .map(ComplimentRepository.ReceivedRankingProjection::getUserId)
                .toList();

        Map<Long, UserRepository.UserNameView> userMap = userRepository.findByIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(UserRepository.UserNameView::getId, v -> v));

        AtomicInteger rankCounter = new AtomicInteger(1);

        return projections.stream()
                .map(p -> {
                    var view = userMap.get(p.getUserId());
                    String name = view != null ? view.getName() : null;
                    String avatarUrl = view != null ? view.getAvatarUrl() : null;

                    return new RankingDtos.ClubRankRes(
                            rankCounter.getAndIncrement(),
                            p.getUserId(),
                            name,
                            avatarUrl,
                            p.getReceivedCount()
                    );
                })
                .toList();
    }


    //  전체에서 많이 보낸 사람 랭킹

    public List<RankingDtos.GlobalSentRankRes> getGlobalSentRanking(int limit) {
        var projections = complimentRepository.findGlobalSentRanking(
                PageRequest.of(0, limit)
        );

        List<Long> userIds = projections.stream()
                .map(ComplimentRepository.SentRankingProjection::getUserId)
                .toList();

        Map<Long, UserRepository.UserNameView> userMap = userRepository.findByIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(UserRepository.UserNameView::getId, v -> v));

        AtomicInteger rankCounter = new AtomicInteger(1);

        return projections.stream()
                .map(p -> {
                    var view = userMap.get(p.getUserId());
                    String name = view != null ? view.getName() : null;
                    String avatarUrl = view != null ? view.getAvatarUrl() : null;

                    return new RankingDtos.GlobalSentRankRes(
                            rankCounter.getAndIncrement(),
                            p.getUserId(),
                            name,
                            avatarUrl,
                            p.getSentCount()
                    );
                })
                .toList();
    }


    // 동아리별로 많이 보낸 사람 랭킹

    public List<RankingDtos.ClubSentRankRes> getClubSentRanking(Long clubId, int limit) {
        var projections = complimentRepository.findClubSentRanking(
                clubId,
                PageRequest.of(0, limit)
        );

        List<Long> userIds = projections.stream()
                .map(ComplimentRepository.SentRankingProjection::getUserId)
                .toList();

        Map<Long, UserRepository.UserNameView> userMap = userRepository.findByIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(UserRepository.UserNameView::getId, v -> v));

        AtomicInteger rankCounter = new AtomicInteger(1);

        return projections.stream()
                .map(p -> {
                    var view = userMap.get(p.getUserId());
                    String name = view != null ? view.getName() : null;
                    String avatarUrl = view != null ? view.getAvatarUrl() : null;

                    return new RankingDtos.ClubSentRankRes(
                            rankCounter.getAndIncrement(),
                            p.getUserId(),
                            name,
                            avatarUrl,
                            p.getSentCount()
                    );
                })
                .toList();
    }
}


