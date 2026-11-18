package br.com.naepoca.etl.repo;

import br.com.naepoca.etl.domain.EtlRaw;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EtlRawRepository extends JpaRepository<EtlRaw, Long> {}
