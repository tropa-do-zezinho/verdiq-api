package br.com.naepoca.market.domain;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;
@Entity @Table(name="ipca_mensal")
public class IpcaMensal {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="ano_mes", nullable=false, unique=true, length=7) private String anoMes;
  @Column(nullable=false) private BigDecimal indice;
  @Column(nullable=false) private String fonte = "BCB_SGS_433";
  @Column(name="coletado_em") private Instant coletadoEm = Instant.now();
  public Long getId(){return id;} public String getAnoMes(){return anoMes;} public void setAnoMes(String v){anoMes=v;}
  public BigDecimal getIndice(){return indice;} public void setIndice(BigDecimal v){indice=v;}
  public String getFonte(){return fonte;} public void setFonte(String v){fonte=v;}
}
