package ssu.sokdak.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import ssu.sokdak.user.domain.User;
import ssu.sokdak.user.domain.UserCategorySelection;

import java.time.LocalDateTime;
import java.util.List;

public class MemberDtos {

    public record RegisterReq(
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotBlank String name,
            String nickname,
            String avatarUrl,
            List<CategorySelectionReq> selections
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

    public record MemberRes(
            Long id, String email, String name, String nickname, String avatarUrl, String status
    ) {
        public static MemberRes from(User u){
            return new MemberRes(u.getId(), u.getEmail(), u.getName(), u.getNickname(), u.getAvatarUrl(), u.getStatus());
        }
    }

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
