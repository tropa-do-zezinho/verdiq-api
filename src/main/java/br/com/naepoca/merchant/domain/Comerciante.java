package br.com.naepoca.merchant.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="comerciante")
public class Comerciante {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="usuario_id", nullable=false, unique=true) private Long usuarioId;
  @Column(name="nome_fantasia", nullable=false) private String nomeFantasia;
  @Column(name="tipo_negocio", nullable=false) private String tipoNegocio;
  @Column(name="praca_id") private Long pracaId;
  @Column(nullable=false) private String plano = "ESSENCIAL";
  private Double latitude; private Double longitude;
  @Column(name="criado_em") private Instant criadoEm = Instant.now();
  public Long getId(){return id;} public Long getUsuarioId(){return usuarioId;}
  public void setUsuarioId(Long v){usuarioId=v;} public String getNomeFantasia(){return nomeFantasia;}
  public void setNomeFantasia(String v){nomeFantasia=v;} public String getTipoNegocio(){return tipoNegocio;}
  public void setTipoNegocio(String v){tipoNegocio=v;} public Long getPracaId(){return pracaId;}
  public void setPracaId(Long v){pracaId=v;} public String getPlano(){return plano;}
  public void setPlano(String v){plano=v;} public Double getLatitude(){return latitude;}
  public void setLatitude(Double v){latitude=v;} public Double getLongitude(){return longitude;}
  public void setLongitude(Double v){longitude=v;}
}
