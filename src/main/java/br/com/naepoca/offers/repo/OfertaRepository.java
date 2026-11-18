package br.com.naepoca.offers.repo;

import br.com.naepoca.offers.domain.Oferta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OfertaRepository extends JpaRepository<Oferta, Long> {
  List<Oferta> findByStatusAndFimGreaterThanEqual(String status, java.time.LocalDate hoje);
  List<Oferta> findByComercianteIdOrderByCriadoEmDesc(Long comercianteId);
}
