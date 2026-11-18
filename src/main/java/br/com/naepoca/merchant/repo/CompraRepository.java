package br.com.naepoca.merchant.repo;

import br.com.naepoca.merchant.domain.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Long> {
  List<Compra> findByComercianteIdAndDataRefBetween(Long comercianteId, java.time.LocalDate ini, java.time.LocalDate fim);
  List<Compra> findByComercianteIdOrderByDataRefDesc(Long comercianteId);
  List<Compra> findByLoteEnvioId(Long loteEnvioId);
}
