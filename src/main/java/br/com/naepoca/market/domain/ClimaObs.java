package br.com.naepoca.market.domain;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.time.LocalDate;
@Entity @Table(name="clima_obs")
public class ClimaObs {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="estacao_codigo", nullable=false) private String estacaoCodigo;
  @Column(name="data_ref", nullable=false) private LocalDate dataRef;
  @Column(name="chuva_mm") private BigDecimal chuvaMm; @Column(name="temp_min") private BigDecimal tempMin;
  @Column(name="temp_max") private BigDecimal tempMax; @Column(nullable=false) private String fonte="UPLOAD";
  @Column(name="coletado_em") private Instant coletadoEm = Instant.now();
  public Long getId(){return id;} public String getEstacaoCodigo(){return estacaoCodigo;} public void setEstacaoCodigo(String v){estacaoCodigo=v;}
  public LocalDate getDataRef(){return dataRef;} public void setDataRef(LocalDate v){dataRef=v;}
  public BigDecimal getChuvaMm(){return chuvaMm;} public void setChuvaMm(BigDecimal v){chuvaMm=v;}
  public BigDecimal getTempMin(){return tempMin;} public void setTempMin(BigDecimal v){tempMin=v;}
  public BigDecimal getTempMax(){return tempMax;} public void setTempMax(BigDecimal v){tempMax=v;}
  public String getFonte(){return fonte;} public void setFonte(String v){fonte=v;}
}
