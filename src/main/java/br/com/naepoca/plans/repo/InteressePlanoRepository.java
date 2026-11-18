package br.com.naepoca.plans.repo;

import br.com.naepoca.plans.domain.InteressePlano;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InteressePlanoRepository extends JpaRepository<InteressePlano, Long> {}
