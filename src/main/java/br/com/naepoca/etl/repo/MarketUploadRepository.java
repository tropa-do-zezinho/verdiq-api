package br.com.naepoca.etl.repo;

import br.com.naepoca.etl.domain.MarketUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MarketUploadRepository extends JpaRepository<MarketUpload, Long> { List<MarketUpload> findTop50ByOrderByCriadoEmDesc(); }
