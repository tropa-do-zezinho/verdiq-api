package br.com.naepoca.market.domain;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.time.LocalDate;
@Entity @Table(name="preco_mercado")
public class PrecoMercado {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="produto_id", nullable=false) private Long produtoId;
  @Column(name="praca_id", nullable=false) private Long pracaId;
  @Column(name="data_ref", nullable=false) private LocalDate dataRef;
  @Column(name="preco_medio", nullable=false) private BigDecimal precoMedio;
  @Column(name="volume_kg") private BigDecimal volumeKg;
  @Column(nullable=false) private String fonte;
  @Column(name="coletado_em") private Instant coletadoEm = Instant.now();
  public Long getId(){return id;} public Long getProdutoId(){return produtoId;} public void setProdutoId(Long v){produtoId=v;}
  public Long getPracaId(){return pracaId;} public void setPracaId(Long v){pracaId=v;}
  public LocalDate getDataRef(){return dataRef;} public void setDataRef(LocalDate v){dataRef=v;}
  public BigDecimal getPrecoMedio(){return precoMedio;} public void setPrecoMedio(BigDecimal v){precoMedio=v;}
  public BigDecimal getVolumeKg(){return volumeKg;} public void setVolumeKg(BigDecimal v){volumeKg=v;}
  public String getFonte(){return fonte;} public void setFonte(String v){fonte=v;}
  public Instant getColetadoEm(){return coletadoEm;}
}
