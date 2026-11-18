package br.com.naepoca.catalog.repo;

import br.com.naepoca.catalog.domain.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByAtivoTrue();

    @Query("""
            select p from Produto p
            where p.ativo = true
              and (:categoria is null or p.categoria = :categoria)
              and (
                   :q is null
                   or lower(p.nomeCanonico) like lower(concat('%', cast(:q as string), '%'))
                   or exists (
                        select 1 from ProdutoSinonimo s
                        where s.produtoId = p.id
                          and lower(s.sinonimo) like lower(concat('%', cast(:q as string), '%'))
                   )
              )
            """)
    List<Produto> search(@Param("q") String q, @Param("categoria") String categoria);
}
