package _2.ArtFusion.repository.jpa;

import _2.ArtFusion.config.jwt.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token>findByEmail(String email);
}
