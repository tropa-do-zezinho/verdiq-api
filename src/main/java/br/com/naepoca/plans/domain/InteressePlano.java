package br.com.naepoca.plans.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="interesse_plano")
public class InteressePlano {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="usuario_id", nullable=false) private Long usuarioId;
  @Column(name="plano_id", nullable=false) private Long planoId;
  @Column(name="criado_em") private Instant criadoEm = Instant.now();
  public Long getId(){return id;} public Long getUsuarioId(){return usuarioId;} public void setUsuarioId(Long v){usuarioId=v;}
  public Long getPlanoId(){return planoId;} public void setPlanoId(Long v){planoId=v;}
  public Instant getCriadoEm(){return criadoEm;}
}
