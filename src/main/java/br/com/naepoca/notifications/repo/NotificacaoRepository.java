package br.com.naepoca.notifications.repo;

import br.com.naepoca.notifications.domain.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> { List<Notificacao> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId); }
