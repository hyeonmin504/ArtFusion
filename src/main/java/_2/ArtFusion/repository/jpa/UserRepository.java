package _2.ArtFusion.repository.jpa;

import _2.ArtFusion.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Boolean existsUserByEmail(String email);

    Optional<User> findByEmail(String email);
}
