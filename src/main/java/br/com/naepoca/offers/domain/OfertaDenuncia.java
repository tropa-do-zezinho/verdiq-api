package br.com.naepoca.offers.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="oferta_denuncia")
public class OfertaDenuncia {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="oferta_id", nullable=false) private Long ofertaId;
  @Column(name="usuario_id", nullable=false) private Long usuarioId;
  private String motivo; @Column(name="criado_em") private Instant criadoEm = Instant.now();
  public Long getId(){return id;} public Long getOfertaId(){return ofertaId;} public void setOfertaId(Long v){ofertaId=v;}
  public Long getUsuarioId(){return usuarioId;} public void setUsuarioId(Long v){usuarioId=v;}
  public String getMotivo(){return motivo;} public void setMotivo(String v){motivo=v;}
}
