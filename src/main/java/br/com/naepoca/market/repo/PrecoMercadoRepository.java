package br.com.naepoca.market.repo;

import br.com.naepoca.market.domain.PrecoMercado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PrecoMercadoRepository extends JpaRepository<PrecoMercado, Long> {
  List<PrecoMercado> findByProdutoIdAndPracaIdAndDataRefBetweenOrderByDataRefAsc(Long produtoId, Long pracaId, java.time.LocalDate ini, java.time.LocalDate fim);
  Optional<PrecoMercado> findFirstByProdutoIdAndPracaIdOrderByDataRefDesc(Long produtoId, Long pracaId);
}
