package br.com.naepoca.notifications.repo;

import br.com.naepoca.notifications.domain.PreferenciaNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PreferenciaNotificacaoRepository extends JpaRepository<PreferenciaNotificacao, Long> { Optional<PreferenciaNotificacao> findByUsuarioId(Long usuarioId); }
