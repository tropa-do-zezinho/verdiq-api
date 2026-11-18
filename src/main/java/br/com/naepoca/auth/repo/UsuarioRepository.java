package br.com.naepoca.auth.repo;

import br.com.naepoca.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCase(String email);
    Optional<Usuario> findByTelefone(String telefone);
    boolean existsByEmailIgnoreCase(String email);
}
