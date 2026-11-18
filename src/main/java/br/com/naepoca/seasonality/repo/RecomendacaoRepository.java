package br.com.naepoca.seasonality.repo;

import br.com.naepoca.seasonality.domain.Recomendacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RecomendacaoRepository extends JpaRepository<Recomendacao, Long> {
  List<Recomendacao> findByComercianteIdOrderByGeradoEmDesc(Long comercianteId);
  List<Recomendacao> findByComercianteIdIsNullOrderByGeradoEmDesc();
}
