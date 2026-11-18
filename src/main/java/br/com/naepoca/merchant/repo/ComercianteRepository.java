package br.com.naepoca.merchant.repo;

import br.com.naepoca.merchant.domain.Comerciante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ComercianteRepository extends JpaRepository<Comerciante, Long> { Optional<Comerciante> findByUsuarioId(Long usuarioId); }
