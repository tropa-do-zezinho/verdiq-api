package br.com.naepoca.notifications.service;

import br.com.naepoca.config.NaEpocaProperties;
import br.com.naepoca.notifications.domain.Notificacao;
import br.com.naepoca.notifications.domain.PreferenciaNotificacao;
import br.com.naepoca.notifications.domain.WebPushSubscription;
import br.com.naepoca.notifications.repo.NotificacaoRepository;
import br.com.naepoca.notifications.repo.PreferenciaNotificacaoRepository;
import br.com.naepoca.notifications.repo.WebPushSubscriptionRepository;
import br.com.naepoca.shared.exception.ApiException;
import br.com.naepoca.shared.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final PreferenciaNotificacaoRepository preferenciaRepository;
    private final WebPushSubscriptionRepository subscriptionRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final NaEpocaProperties properties;

    public NotificationService(
            PreferenciaNotificacaoRepository preferenciaRepository,
            WebPushSubscriptionRepository subscriptionRepository,
            NotificacaoRepository notificacaoRepository,
            NaEpocaProperties properties
    ) {
        this.preferenciaRepository = preferenciaRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public PreferenciaNotificacao getPreferences() {
        return preferenciaRepository.findByUsuarioId(SecurityUtils.currentUserId())
                .orElseGet(() -> {
                    PreferenciaNotificacao p = new PreferenciaNotificacao();
                    p.setUsuarioId(SecurityUtils.currentUserId());
                    return p;
                });
    }

    @Transactional
    public PreferenciaNotificacao updatePreferences(Map<String, Object> body) {
        PreferenciaNotificacao p = preferenciaRepository.findByUsuarioId(SecurityUtils.currentUserId())
                .orElseGet(() -> {
                    PreferenciaNotificacao n = new PreferenciaNotificacao();
                    n.setUsuarioId(SecurityUtils.currentUserId());
                    return n;
                });
        if (body.get("canalWebPush") != null) p.setCanalWebPush(Boolean.valueOf(String.valueOf(body.get("canalWebPush"))));
        if (body.get("canalInApp") != null) p.setCanalInApp(Boolean.valueOf(String.valueOf(body.get("canalInApp"))));
        if (body.get("maxPorSemana") != null) p.setMaxPorSemana(Integer.valueOf(String.valueOf(body.get("maxPorSemana"))));
        if (body.get("horaInicio") != null) p.setHoraInicio(Integer.valueOf(String.valueOf(body.get("horaInicio"))));
        if (body.get("horaFim") != null) p.setHoraFim(Integer.valueOf(String.valueOf(body.get("horaFim"))));
        if (body.get("tiposAlerta") != null) p.setTiposAlerta(String.valueOf(body.get("tiposAlerta")));
        return preferenciaRepository.save(p);
    }

    @Transactional
    public WebPushSubscription subscribe(Map<String, String> body) {
        if (body.get("endpoint") == null || body.get("p256dh") == null || body.get("auth") == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SUB", "endpoint, p256dh e auth são obrigatórios");
        }
        WebPushSubscription sub = subscriptionRepository.findByEndpoint(body.get("endpoint"))
                .orElseGet(WebPushSubscription::new);
        sub.setUsuarioId(SecurityUtils.currentUserId());
        sub.setEndpoint(body.get("endpoint"));
        sub.setP256dh(body.get("p256dh"));
        sub.setAuth(body.get("auth"));
        PreferenciaNotificacao pref = getPreferences();
        pref.setCanalWebPush(true);
        preferenciaRepository.save(pref);
        return subscriptionRepository.save(sub);
    }

    @Transactional(readOnly = true)
    public Map<String, String> vapidPublicKey() {
        String key = properties.webpush().publicKey();
        return Map.of("publicKey", key == null ? "" : key);
    }

    @Transactional(readOnly = true)
    public List<Notificacao> inbox() {
        return notificacaoRepository.findByUsuarioIdOrderByCriadoEmDesc(SecurityUtils.currentUserId());
    }

    @Transactional
    public void markRead(Long id) {
        Notificacao n = notificacaoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Notificação não encontrada"));
        if (!n.getUsuarioId().equals(SecurityUtils.currentUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Notificação de outro usuário");
        }
        n.setLida(true);
    }

    @Transactional
    public Notificacao createInApp(Long usuarioId, String titulo, String corpo, String tipo) {
        Notificacao n = new Notificacao();
        n.setUsuarioId(usuarioId);
        n.setTitulo(titulo);
        n.setCorpo(corpo);
        n.setTipo(tipo);
        return notificacaoRepository.save(n);
    }
}
