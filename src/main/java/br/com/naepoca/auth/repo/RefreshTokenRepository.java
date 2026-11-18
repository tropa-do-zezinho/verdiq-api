package br.com.naepoca.auth.repo;

import br.com.naepoca.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHashAndRevogadoFalse(String tokenHash);
}
