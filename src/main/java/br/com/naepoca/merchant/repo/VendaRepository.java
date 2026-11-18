package br.com.naepoca.merchant.repo;

import br.com.naepoca.merchant.domain.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {
  List<Venda> findByComercianteIdAndDataRefBetween(Long comercianteId, java.time.LocalDate ini, java.time.LocalDate fim);
  List<Venda> findByComercianteIdOrderByDataRefDesc(Long comercianteId);
  List<Venda> findByLoteEnvioId(Long loteEnvioId);
}
