package br.com.naepoca.catalog.repo;

import br.com.naepoca.catalog.domain.ProdutoSinonimo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProdutoSinonimoRepository extends JpaRepository<ProdutoSinonimo, Long> { List<ProdutoSinonimo> findByProdutoId(Long produtoId); }
