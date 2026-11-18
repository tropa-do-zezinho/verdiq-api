package br.com.naepoca.notifications.web;

import br.com.naepoca.notifications.domain.Notificacao;
import br.com.naepoca.notifications.domain.PreferenciaNotificacao;
import br.com.naepoca.notifications.domain.WebPushSubscription;
import br.com.naepoca.notifications.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
@Tag(name = "Notificações")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/me/notification-preferences")
    public PreferenciaNotificacao getPrefs() {
        return notificationService.getPreferences();
    }

    @PutMapping("/me/notification-preferences")
    public PreferenciaNotificacao putPrefs(@RequestBody Map<String, Object> body) {
        return notificationService.updatePreferences(body);
    }

    @GetMapping("/me/webpush/vapid-public-key")
    public Map<String, String> vapid() {
        return notificationService.vapidPublicKey();
    }

    @PostMapping("/me/webpush/subscriptions")
    @ResponseStatus(HttpStatus.CREATED)
    public WebPushSubscription subscribe(@RequestBody Map<String, String> body) {
        return notificationService.subscribe(body);
    }

    @GetMapping("/me/notifications")
    public List<Notificacao> inbox() {
        return notificationService.inbox();
    }

    @PostMapping("/me/notifications/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void read(@PathVariable Long id) {
        notificationService.markRead(id);
    }
}
