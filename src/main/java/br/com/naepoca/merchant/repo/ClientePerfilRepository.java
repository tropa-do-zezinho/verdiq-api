package br.com.naepoca.merchant.repo;

import br.com.naepoca.merchant.domain.ClientePerfil;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClientePerfilRepository extends JpaRepository<ClientePerfil, Long> { Optional<ClientePerfil> findByUsuarioId(Long usuarioId); }
