package br.com.naepoca.notifications.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="notificacao")
public class Notificacao {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="usuario_id", nullable=false) private Long usuarioId;
  @Column(nullable=false) private String titulo;
  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CLOB)
  @Column(nullable=false) private String corpo;
  @Column(nullable=false) private String tipo;
  @Column(nullable=false) private Boolean lida = false;
  @Column(name="criado_em") private Instant criadoEm = Instant.now();
  public Long getId(){return id;} public Long getUsuarioId(){return usuarioId;} public void setUsuarioId(Long v){usuarioId=v;}
  public String getTitulo(){return titulo;} public void setTitulo(String v){titulo=v;}
  public String getCorpo(){return corpo;} public void setCorpo(String v){corpo=v;}
  public String getTipo(){return tipo;} public void setTipo(String v){tipo=v;}
  public Boolean getLida(){return lida;} public void setLida(Boolean v){lida=v;}
  public Instant getCriadoEm(){return criadoEm;}
}
