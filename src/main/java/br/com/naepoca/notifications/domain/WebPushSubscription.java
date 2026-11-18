package br.com.naepoca.notifications.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="web_push_subscription")
public class WebPushSubscription {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="usuario_id", nullable=false) private Long usuarioId;
  @Column(nullable=false, unique=true) private String endpoint;
  @Column(nullable=false) private String p256dh;
  @Column(nullable=false) private String auth;
  @Column(name="criado_em") private Instant criadoEm = Instant.now();
  public Long getId(){return id;} public Long getUsuarioId(){return usuarioId;} public void setUsuarioId(Long v){usuarioId=v;}
  public String getEndpoint(){return endpoint;} public void setEndpoint(String v){endpoint=v;}
  public String getP256dh(){return p256dh;} public void setP256dh(String v){p256dh=v;}
  public String getAuth(){return auth;} public void setAuth(String v){auth=v;}
}
