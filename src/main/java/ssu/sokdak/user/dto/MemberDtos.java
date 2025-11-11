package ssu.sokdak.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import ssu.sokdak.user.domain.User;

public class MemberDtos {

    public record RegisterReq(
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotBlank String name,
            String nickname,
            String avatarUrl
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
}
