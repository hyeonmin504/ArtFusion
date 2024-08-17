package _2.ArtFusion.repository.jpa;

import _2.ArtFusion.domain.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token>findByEmail(String email);

    boolean existsByAccessToken(String accessToken);

    void deleteByAccessToken(String accessToken);


}
