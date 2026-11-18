package br.com.naepoca.etl.repo;

import br.com.naepoca.etl.domain.EtlRun;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EtlRunRepository extends JpaRepository<EtlRun, Long> { List<EtlRun> findTop50ByOrderByIniciadoEmDesc(); }
