package ssu.sokdak.club.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ssu.sokdak.club.domain.Club;
import ssu.sokdak.club.domain.ClubMember;
import ssu.sokdak.club.dto.ClubDtos.*;
import ssu.sokdak.club.repository.ClubMemberRepository;
import ssu.sokdak.club.repository.ClubRepository;
import ssu.sokdak.user.domain.User;
import ssu.sokdak.user.repository.UserRepository;

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
    private final UserRepository userRepository;

    public Long createClub(CreateClubRequest req, Long ownerUserId) {
        if (clubRepository.existsByName(req.name())) {
            throw new IllegalArgumentException("이미 존재하는 동아리 이름입니다.");
        }
        Club club = Club.builder().name(req.name()).description(req.description()).build();
        club = clubRepository.save(club);

        User owner = userRepository.getReferenceById(ownerUserId);
        ClubMember manager = ClubMember.builder()
                .club(club).user(owner).role("manager").active(true).joinedAt(LocalDateTime.now()).build();
        clubMemberRepository.save(manager);
        return club.getId();
    }

    public void deleteClub(Long clubId, Long requesterUserId) {
        ClubMember me = clubMemberRepository.findByClubIdAndUserId(clubId, requesterUserId)
                .orElseThrow(() -> new NoSuchElementException("동아리에 속하지 않은 사용자입니다."));
        if (!me.isManager()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "동아리 삭제 권한이 없습니다.");

        clubMemberRepository.deleteAllByClubId(clubId);
        clubRepository.deleteById(clubId);
    }

    @Transactional(readOnly = true)
    public List<ClubSearchResponse> searchClubs(String query) {
        if (query == null || query.isBlank()) return List.of();
        List<Club> clubs = clubRepository.findByNameContainingOrDescriptionContaining(query, query);

        return clubs.stream().map(club -> {
            int count = clubMemberRepository.findActiveMemberIdsByClubId(club.getId()).size();
            return ClubSearchResponse.from(club, count);
        }).toList();
    }

    public JoinClubResponse requestJoin(Long clubId, Long requesterUserId) {
        Club club = clubRepository.findById(clubId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 동아리입니다."));
        User user = userRepository.getReferenceById(requesterUserId);
        if (clubMemberRepository.existsByClubIdAndUserId(clubId, requesterUserId)) {
            throw new IllegalStateException("이미 가입했거나 가입 대기 중인 사용자입니다.");
        }
        ClubMember pending = ClubMember.builder().club(club).user(user).role("member").active(false).build();
        clubMemberRepository.save(pending);
        return new JoinClubResponse(clubId, requesterUserId, "PENDING");
    }

    public ApproveClubMemberResponse approveJoinRequest(Long clubId, Long managerUserId, Long targetUserId) {
        ClubMember me = clubMemberRepository.findByClubIdAndUserId(clubId, managerUserId)
                .orElseThrow(() -> new NoSuchElementException("동아리에 속하지 않은 사용자입니다."));
        if (!me.isManager()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "동아리 가입 승인 권한이 없습니다.");

        ClubMember target = clubMemberRepository.findByClubIdAndUserId(clubId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("가입 내역이 존재하지 않습니다."));
        if (target.isActive()) throw new IllegalStateException("이미 승인된 멤버입니다.");

        target.approve(LocalDateTime.now());
        return new ApproveClubMemberResponse(clubId, targetUserId, "APPROVED");
    }

    public RejectClubMemberResponse rejectJoinRequest(Long clubId, Long managerUserId, Long targetUserId) {
        ClubMember me = clubMemberRepository.findByClubIdAndUserId(clubId, managerUserId)
                .orElseThrow(() -> new NoSuchElementException("동아리에 속하지 않은 사용자입니다."));
        if (!me.isManager()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "동아리 가입 거절 권한이 없습니다.");

        ClubMember target = clubMemberRepository.findByClubIdAndUserId(clubId, targetUserId)
                .orElseThrow(() -> new NoSuchElementException("가입 내역이 존재하지 않습니다."));
        if (target.isActive()) throw new IllegalStateException("이미 승인된 멤버는 거절할 수 없습니다.");

        clubMemberRepository.delete(target);
        return new RejectClubMemberResponse(clubId, targetUserId, "REJECTED");
    }

    // [수정] 상세 조회 시 Role 포함
    @Transactional(readOnly = true)
    public ClubDetailResponse getClubDetail(Long clubId) {
        Club club = clubRepository.findById(clubId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 동아리입니다."));

        // Role까지 한 번에 가져오는 쿼리 사용
        List<ClubMember> activeMembers = clubMemberRepository.findWithUserByClubIdAndActiveTrue(clubId);

        List<ClubDetailMember> members = activeMembers.stream()
                .sorted(Comparator.comparing(cm -> cm.getUser().getName()))
                .map(cm -> ClubDetailMember.of(
                        cm.getUser().getId(),
                        cm.getUser().getName(),
                        cm.getUser().getAvatarUrl(),
                        cm.getRole() // Role 추가
                ))
                .toList();

        return new ClubDetailResponse(club.getId(), club.getName(), club.getDescription(), members.size(), members, club.getCreatedAt(), club.getUpdatedAt());
    }

    // [수정] 멤버 목록 조회 시 Role 포함
    @Transactional(readOnly = true)
    public ClubMembersResponse getActiveMembers(Long clubId) {
        Club club = clubRepository.findById(clubId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 동아리입니다."));

        List<ClubMember> activeMembers = clubMemberRepository.findWithUserByClubIdAndActiveTrue(clubId);

        List<ClubDetailMember> members = activeMembers.stream()
                .sorted(Comparator.comparing(cm -> cm.getUser().getName()))
                .map(cm -> ClubDetailMember.of(
                        cm.getUser().getId(),
                        cm.getUser().getName(),
                        cm.getUser().getAvatarUrl(),
                        cm.getRole() // Role 추가
                ))
                .toList();

        // rankings에 빈 리스트 전달
        return new ClubMembersResponse(club.getId(), true, members.size(), members, List.of());
    }

    // [수정] 대기 멤버 목록 조회 시 Role 포함
    @Transactional(readOnly = true)
    public ClubMembersResponse getPendingMembers(Long clubId, Long managerUserId) {
        Club club = clubRepository.findById(clubId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 동아리입니다."));
        ClubMember me = clubMemberRepository.findByClubIdAndUserId(clubId, managerUserId)
                .orElseThrow(() -> new NoSuchElementException("동아리에 속하지 않은 사용자입니다."));
        if (!me.isManager()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "가입 대기 멤버 조회 권한이 없습니다.");

        List<ClubMember> pendingMembers = clubMemberRepository.findWithUserByClubIdAndActiveFalse(clubId);

        List<ClubDetailMember> members = pendingMembers.stream()
                .sorted(Comparator.comparing(cm -> cm.getUser().getName()))
                .map(cm -> ClubDetailMember.of(
                        cm.getUser().getId(),
                        cm.getUser().getName(),
                        cm.getUser().getAvatarUrl(),
                        cm.getRole()
                ))
                .toList();

        return new ClubMembersResponse(club.getId(), false, members.size(), members, List.of());
    }
}