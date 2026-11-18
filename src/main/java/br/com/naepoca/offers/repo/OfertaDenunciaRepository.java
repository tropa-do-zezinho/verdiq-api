package br.com.naepoca.offers.repo;

import br.com.naepoca.offers.domain.OfertaDenuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OfertaDenunciaRepository extends JpaRepository<OfertaDenuncia, Long> { long countByOfertaId(Long ofertaId); boolean existsByOfertaIdAndUsuarioId(Long ofertaId, Long usuarioId); }
