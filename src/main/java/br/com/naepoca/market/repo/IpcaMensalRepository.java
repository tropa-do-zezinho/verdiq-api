package br.com.naepoca.market.repo;

import br.com.naepoca.market.domain.IpcaMensal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IpcaMensalRepository extends JpaRepository<IpcaMensal, Long> { Optional<IpcaMensal> findByAnoMes(String anoMes); List<IpcaMensal> findAllByOrderByAnoMesAsc(); }
