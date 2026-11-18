package br.com.naepoca.catalog.repo;

import br.com.naepoca.catalog.domain.Praca;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PracaRepository extends JpaRepository<Praca, Long> { Optional<Praca> findByCodigo(String codigo); }
