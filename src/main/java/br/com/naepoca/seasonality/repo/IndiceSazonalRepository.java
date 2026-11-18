package br.com.naepoca.seasonality.repo;

import br.com.naepoca.seasonality.domain.IndiceSazonal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IndiceSazonalRepository extends JpaRepository<IndiceSazonal, Long> {
  List<IndiceSazonal> findByProdutoIdAndPracaIdAndTipo(Long produtoId, Long pracaId, String tipo);
  List<IndiceSazonal> findByProdutoIdAndTipoAndComercianteIdIsNull(Long produtoId, String tipo);
  List<IndiceSazonal> findByComercianteIdAndTipo(Long comercianteId, String tipo);
  void deleteByTipoAndComercianteIdIsNull(String tipo);
}
