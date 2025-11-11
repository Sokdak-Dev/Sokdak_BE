package ssu.sokdak.user.service;

import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssu.sokdak.user.domain.User;
import ssu.sokdak.user.dto.MemberDtos;
import ssu.sokdak.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final UserRepository userRepository;

    @Transactional
    public User register(MemberDtos.RegisterReq req){
        if (userRepository.existsByEmail(req.email()))
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        String hash = BCrypt.hashpw(req.password(), BCrypt.gensalt(12));
        User user = User.builder()
                .email(req.email())
                .passwordHash(hash)
                .name(req.name())
                .nickname(req.nickname())
                .avatarUrl(req.avatarUrl())
                .status("active")
                .build();
        return userRepository.save(user);
    }

    public User login(MemberDtos.LoginReq req){
        User u = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));
        if (!BCrypt.checkpw(req.password(), u.getPasswordHash()))
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        if (!"active".equals(u.getStatus()))
            throw new IllegalStateException("비활성화된 계정입니다.");
        return u;
    }

    public User get(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
    }

    @Transactional
    public User update(Long id, MemberDtos.UpdateReq req){
        User u = get(id);
        User updated = User.builder()
                .id(u.getId())
                .email(u.getEmail())
                .passwordHash(u.getPasswordHash())
                .name(req.name()!=null ? req.name() : u.getName())
                .nickname(req.nickname()!=null ? req.nickname() : u.getNickname())
                .avatarUrl(req.avatarUrl()!=null ? req.avatarUrl() : u.getAvatarUrl())
                .status(u.getStatus())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
        return userRepository.save(updated);
    }

    @Transactional
    public void deactivate(Long id){
        User u = get(id);
        User deactivated = User.builder()
                .id(u.getId())
                .email(u.getEmail())
                .passwordHash(u.getPasswordHash())
                .name(u.getName())
                .nickname(u.getNickname())
                .avatarUrl(u.getAvatarUrl())
                .status("deactivated")
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
        userRepository.save(deactivated);
    }
}
