package br.com.naepoca.admin.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="audit_log")
public class AuditLog {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="usuario_id") private Long usuarioId;
  @Column(nullable=false) private String acao;
  private String detalhe; @Column(name="criado_em") private Instant criadoEm = Instant.now();
  public Long getId(){return id;} public Long getUsuarioId(){return usuarioId;} public void setUsuarioId(Long v){usuarioId=v;}
  public String getAcao(){return acao;} public void setAcao(String v){acao=v;}
  public String getDetalhe(){return detalhe;} public void setDetalhe(String v){detalhe=v;}
  public Instant getCriadoEm(){return criadoEm;}
}
