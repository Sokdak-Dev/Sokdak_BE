package ssu.sokdak.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssu.sokdak.user.domain.User;
import java.util.Collection;
import java.util.List;



import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    public interface UserNameView {
        Long getId();
        String getName();
    }

    List<UserNameView> findByIdIn(Collection<Long> ids);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}

