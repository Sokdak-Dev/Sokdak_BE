package ssu.sokdak.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssu.sokdak.user.domain.User;

// ClubService에서 참조해야해서 임시로 추가해둠 - 구현 시 수정ㄱㄱ
public interface UserRepository extends JpaRepository<User, Long> { }