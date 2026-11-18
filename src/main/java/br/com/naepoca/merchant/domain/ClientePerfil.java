package br.com.naepoca.merchant.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="cliente_perfil")
public class ClientePerfil {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="usuario_id", nullable=false, unique=true) private Long usuarioId;
  private String cidade; @Column(length=2) private String uf="SP";
  @Column(name="criado_em") private Instant criadoEm = Instant.now();
  public Long getId(){return id;} public Long getUsuarioId(){return usuarioId;}
  public void setUsuarioId(Long v){usuarioId=v;} public String getCidade(){return cidade;}
  public void setCidade(String v){cidade=v;} public String getUf(){return uf;} public void setUf(String v){uf=v;}
}
