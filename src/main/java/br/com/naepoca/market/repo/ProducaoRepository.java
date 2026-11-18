package br.com.naepoca.market.repo;

import br.com.naepoca.market.domain.Producao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProducaoRepository extends JpaRepository<Producao, Long> {
    List<Producao> findByProdutoId(Long produtoId);
    Optional<Producao> findByProdutoIdAndUfAndAnoAndFonte(Long produtoId, String uf, Integer ano, String fonte);
    List<Producao> findByProdutoIdOrderByAnoDesc(Long produtoId);
}
