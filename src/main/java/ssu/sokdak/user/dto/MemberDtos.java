package ssu.sokdak.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import ssu.sokdak.user.domain.User;
import ssu.sokdak.club.dto.ClubDtos.ClubSimpleRes; // [HEAD: 내 코드]
import ssu.sokdak.user.domain.UserCategorySelection; // [develop: 팀원 코드]
import java.time.LocalDateTime; // [develop: 팀원 코드]

public class MemberDtos {

    public record RegisterReq(
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotBlank String name,
            String nickname,
            String avatarUrl,
            List<CategorySelectionReq> selections // develop에서 추가됨 (유지)
    ) {}

    public record CategorySelectionReq(
            @NotBlank String categoryCode,
            @NotBlank String optionLabel,
            Integer rank
    ) {}

    public record LoginReq(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record UpdateReq(
            String name,
            String nickname,
            String avatarUrl
    ) {}

    // [HEAD: 내 코드] clubs 필드가 있는 버전 사용
    public record MemberRes(
            Long id,
            String email,
            String name,
            String nickname,
            String avatarUrl,
            String status,
            List<ClubSimpleRes> clubs // 가입한 동아리 목록
    ) {
        public static MemberRes from(User u, List<ClubSimpleRes> clubs){
            return new MemberRes(
                    u.getId(),
                    u.getEmail(),
                    u.getName(),
                    u.getNickname(),
                    u.getAvatarUrl(),
                    u.getStatus(),
                    clubs
            );
        }
    }

    // [develop: 팀원 코드] 새로 추가된 레코드 유지
    public record CategorySelectionRes(
            Long categoryId,
            String categoryCode,
            Long optionId,
            String optionLabel,
            Integer rank,
            LocalDateTime selectedAt
    ) {
        public static CategorySelectionRes from(UserCategorySelection selection) {
            return new CategorySelectionRes(
                    selection.getCategory().getId(),
                    selection.getCategory().getCode(),
                    selection.getOption().getId(),
                    selection.getOption().getLabel(),
                    selection.getRank(),
                    selection.getSelectedAt()
            );
        }
    }
}