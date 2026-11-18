package br.com.naepoca.market.repo;

import br.com.naepoca.market.domain.ClimaObs;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClimaObsRepository extends JpaRepository<ClimaObs, Long> {}
