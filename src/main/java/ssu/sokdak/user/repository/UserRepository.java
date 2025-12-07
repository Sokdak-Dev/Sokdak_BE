package ssu.sokdak.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssu.sokdak.user.domain.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이름과 함께 프사도 가져오도록 수정
    public interface UserNameView {
        Long getId();
        String getName();
        String getAvatarUrl(); // UI에 프로필 표시를 위해 추가
    }

    List<UserNameView> findByIdIn(Collection<Long> ids);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}