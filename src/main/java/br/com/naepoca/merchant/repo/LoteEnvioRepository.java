package br.com.naepoca.merchant.repo;

import br.com.naepoca.merchant.domain.LoteEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LoteEnvioRepository extends JpaRepository<LoteEnvio, Long> { List<LoteEnvio> findByComercianteIdOrderByCriadoEmDesc(Long comercianteId); }
