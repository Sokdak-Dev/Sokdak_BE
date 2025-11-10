package ssu.sokdak.club.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ssu.sokdak.club.domain.Club;
import ssu.sokdak.club.domain.ClubMember;
import ssu.sokdak.club.dto.ClubDtos.CreateClubRequest;
import ssu.sokdak.club.repository.ClubMemberRepository;
import ssu.sokdak.club.repository.ClubRepository;
import ssu.sokdak.user.domain.User;
import ssu.sokdak.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final UserRepository userRepository; // 인증 미도입, getReferenceById 로 영속 참조만 가져옴

    // 동아리 생성
    public Long createClub(CreateClubRequest req, Long ownerUserId) {
        // 1) 이름 중복 방지
        if (clubRepository.existsByName(req.name())) {
            throw new IllegalArgumentException("이미 존재하는 동아리 이름입니다.");
        }

        // 2) Club 엔티티 생성 및 저장 - 기본생성자 protected라서 롬복 빌더로 구현
        Club club = Club.builder()
                .name(req.name())
                .description(req.description())
                .build();
        club = clubRepository.save(club);

        // 3) 초기 생성자가 manager, 요청에 대하여 active=true 로 ClubMember 생성
        User owner = userRepository.getReferenceById(ownerUserId);
        ClubMember manager = ClubMember.builder()
                .club(club)
                .user(owner)                      // 동아리 만든 manager
                .role("manager")                  // 개설자는 관리자 역할
                .active(true)                     // 승인 시, true로 변환
                .joinedAt(LocalDateTime.now())
                .build();
        clubMemberRepository.save(manager);

        return club.getId();
    }

    // 동아리 삭제
    public void deleteClub(Long clubId, Long requesterUserId) {
        // 1) 요청자 ClubMember 조회 - 동아리에 소속되었는지 확인
        ClubMember me = clubMemberRepository.findByClubIdAndUserId(clubId, requesterUserId)
                .orElseThrow(() -> new NoSuchElementException("동아리에 속하지 않은 사용자입니다."));

        // 2) 권한 확인 후 manager만 삭제 가능, 보안 라이브러리 말고 403 던지도록 처리함
        if (!"manager".equals(me.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "동아리 삭제 권한이 없습니다.");
        }

        // 3) 멤버 → 클럽 순으로 삭제
        clubMemberRepository.deleteAllByClubId(clubId);
        clubRepository.deleteById(clubId);
    }
}