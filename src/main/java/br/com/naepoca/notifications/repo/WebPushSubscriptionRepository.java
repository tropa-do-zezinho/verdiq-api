package br.com.naepoca.notifications.repo;

import br.com.naepoca.notifications.domain.WebPushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WebPushSubscriptionRepository extends JpaRepository<WebPushSubscription, Long> { List<WebPushSubscription> findByUsuarioId(Long usuarioId); Optional<WebPushSubscription> findByEndpoint(String endpoint); }
