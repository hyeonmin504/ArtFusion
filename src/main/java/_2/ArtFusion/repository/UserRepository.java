package _2.ArtFusion.repository;

import _2.ArtFusion.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
