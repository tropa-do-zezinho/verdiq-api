package br.com.naepoca.merchant.repo;

import br.com.naepoca.merchant.domain.Perda;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PerdaRepository extends JpaRepository<Perda, Long> {
  List<Perda> findByComercianteIdAndDataRefBetween(Long comercianteId, java.time.LocalDate ini, java.time.LocalDate fim);
  List<Perda> findByComercianteIdOrderByDataRefDesc(Long comercianteId);
  List<Perda> findByLoteEnvioId(Long loteEnvioId);
}
