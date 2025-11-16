package ssu.sokdak.club.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ssu.sokdak.club.domain.Club;
import ssu.sokdak.club.domain.ClubMember;
import ssu.sokdak.club.dto.ClubDtos.ApproveClubMemberResponse;
import ssu.sokdak.club.dto.ClubDtos.CreateClubRequest;
import ssu.sokdak.club.dto.ClubDtos.JoinClubResponse;
import ssu.sokdak.club.dto.ClubDtos.RejectClubMemberResponse;
import ssu.sokdak.club.dto.ClubDtos.ClubDetailMember;
import ssu.sokdak.club.dto.ClubDtos.ClubDetailResponse;
import ssu.sokdak.club.repository.ClubMemberRepository;
import ssu.sokdak.club.repository.ClubRepository;
import ssu.sokdak.user.domain.User;
import ssu.sokdak.user.repository.UserRepository;
import ssu.sokdak.user.repository.UserRepository.UserNameView;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
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
                .active(true)                     // 이미 승인된 상태
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
        if (!me.isManager()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "동아리 삭제 권한이 없습니다.");
        }

        // 3) 멤버 → 클럽 순으로 삭제
        clubMemberRepository.deleteAllByClubId(clubId);
        clubRepository.deleteById(clubId);
    }

    // 동아리 가입 신청
    // 신규 row 를 active=false, joinedAt=null 로 생성하여 "대기 상태"로 설정
    public JoinClubResponse requestJoin(Long clubId, Long requesterUserId) {

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 동아리입니다."));

        User user = userRepository.getReferenceById(requesterUserId);

        if (clubMemberRepository.existsByClubIdAndUserId(clubId, requesterUserId)) {
            throw new IllegalStateException("이미 가입했거나 가입 대기 중인 사용자입니다.");
        }

        // 가입 대기 상태 row 생성 (active=false, joinedAt=null)
        ClubMember pending = ClubMember.builder()
                .club(club)
                .user(user)
                .role("member")
                .active(false)       // 대기 상태
                .joinedAt(null)      // 아직 승인 전이므로 null, 승인 후 now()
                .build();

        clubMemberRepository.save(pending);

        return new JoinClubResponse(
                clubId,
                requesterUserId,
                "PENDING"
        );
    }

    // 동아리 가입 승인
    public ApproveClubMemberResponse approveJoinRequest(
            Long clubId,
            Long managerUserId,
            Long targetUserId
    ) {
        // 1) 관리자인지 조회
        ClubMember me = clubMemberRepository.findByClubIdAndUserId(clubId, managerUserId)
                .orElseThrow(() -> new NoSuchElementException("동아리에 속하지 않은 사용자입니다."));

        // 2) 권한 체크: manager 만 승인 가능
        if (!me.isManager()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "동아리 가입 승인 권한이 없습니다.");
        }

        // 3) 승인 대상 멤버 조회
        ClubMember target = clubMemberRepository.findByClubIdAndUserId(clubId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("가입 내역이 존재하지 않습니다."));

        // 4) 이미 승인된 멤버라면, 예외처리
        if (target.isActive()) {
            throw new IllegalStateException("이미 승인된 멤버입니다.");
        }

        // 5) 승인 처리: active=true, joinedAt=now
        target.approve(LocalDateTime.now());

        return new ApproveClubMemberResponse(
                clubId,
                targetUserId,
                "APPROVED"
        );
    }

    // 동아리 가입 거절
    public RejectClubMemberResponse rejectJoinRequest(
            Long clubId,
            Long managerUserId,
            Long targetUserId
    ) {
        // 1) 관리자인지 조회
        ClubMember me = clubMemberRepository.findByClubIdAndUserId(clubId, managerUserId)
                .orElseThrow(() -> new NoSuchElementException("동아리에 속하지 않은 사용자입니다."));

        // 2) 권한 체크
        if (!me.isManager()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "동아리 가입 거절 권한이 없습니다.");
        }

        // 3) 거절 대상 멤버 조회
        ClubMember target = clubMemberRepository.findByClubIdAndUserId(clubId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("가입 내역이 존재하지 않습니다."));

        // 4) 이미 승인된 멤버라면, 강퇴 api 생성 후 별도 처리
        if (target.isActive()) {
            throw new IllegalStateException("이미 승인된 멤버는 거절할 수 없습니다.");
        }

        // 5) 대기 상태 row 삭제 = 거절
        clubMemberRepository.delete(target);

        return new RejectClubMemberResponse(
                clubId,
                targetUserId,
                "REJECTED"
        );
    }

    // 동아리 상세 조회 -> Club 엔티티 + 승인된 멤버수 + 리스트
    @Transactional(readOnly = true)
    public ClubDetailResponse getClubDetail(Long clubId) {
        // 1) 동아리 존재 여부 확인
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 동아리입니다."));

        // 2) 승인된 멤버의 userId 목록 조회
        List<Long> activeMemberIds = clubMemberRepository.findActiveMemberIdsByClubId(clubId);

        // 3) 승인된 멤버가 없는 경우: 카운트 0, 빈 리스트로 응답
        if (activeMemberIds.isEmpty()) {
            return new ClubDetailResponse(
                    club.getId(),
                    club.getName(),
                    club.getDescription(),
                    0,
                    List.of(),
                    club.getCreatedAt(),
                    club.getUpdatedAt()
            );
        }

        // 4) userId 목록으로 이름 projection 조회
        List<UserNameView> views = userRepository.findByIdIn(activeMemberIds);

        // 5) 이름 오름차순 정렬 후 응답용 멤버 DTO로 매핑
        List<ClubDetailMember> members = views.stream()
                .sorted(Comparator.comparing(UserNameView::getName))
                .map(v -> new ClubDetailMember(v.getId(), v.getName()))
                .toList();

        return new ClubDetailResponse(
                club.getId(),
                club.getName(),
                club.getDescription(),
                members.size(),
                members,
                club.getCreatedAt(),
                club.getUpdatedAt()
        );
    }
}