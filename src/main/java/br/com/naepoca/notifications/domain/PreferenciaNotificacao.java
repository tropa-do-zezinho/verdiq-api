package br.com.naepoca.notifications.domain;
import jakarta.persistence.*;
@Entity @Table(name="preferencia_notificacao")
public class PreferenciaNotificacao {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="usuario_id", nullable=false, unique=true) private Long usuarioId;
  @Column(name="canal_web_push", nullable=false) private Boolean canalWebPush = false;
  @Column(name="canal_in_app", nullable=false) private Boolean canalInApp = true;
  @Column(name="max_por_semana", nullable=false) private Integer maxPorSemana = 3;
  @Column(name="hora_inicio", nullable=false) private Integer horaInicio = 8;
  @Column(name="hora_fim", nullable=false) private Integer horaFim = 20;
  @Column(name="tipos_alerta", nullable=false) private String tiposAlerta = "JANELA_COMPRA,EM_SAFRA,RISCO_PERDA,PRECO_ALTO";
  public Long getId(){return id;} public Long getUsuarioId(){return usuarioId;} public void setUsuarioId(Long v){usuarioId=v;}
  public Boolean getCanalWebPush(){return canalWebPush;} public void setCanalWebPush(Boolean v){canalWebPush=v;}
  public Boolean getCanalInApp(){return canalInApp;} public void setCanalInApp(Boolean v){canalInApp=v;}
  public Integer getMaxPorSemana(){return maxPorSemana;} public void setMaxPorSemana(Integer v){maxPorSemana=v;}
  public Integer getHoraInicio(){return horaInicio;} public void setHoraInicio(Integer v){horaInicio=v;}
  public Integer getHoraFim(){return horaFim;} public void setHoraFim(Integer v){horaFim=v;}
  public String getTiposAlerta(){return tiposAlerta;} public void setTiposAlerta(String v){tiposAlerta=v;}
}
