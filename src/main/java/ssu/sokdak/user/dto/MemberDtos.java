package ssu.sokdak.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import ssu.sokdak.user.domain.User;
import ssu.sokdak.club.dto.ClubDtos.ClubSimpleRes; // 추가

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
            Long id,
            String email,
            String name,
            String nickname,
            String avatarUrl,
            String status,
            List<ClubSimpleRes> clubs // [추가] 가입한 동아리 목록
    ) {
        public static MemberRes from(User u, List<ClubSimpleRes> clubs){ // [수정] clubs 파라미터 추가
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
}