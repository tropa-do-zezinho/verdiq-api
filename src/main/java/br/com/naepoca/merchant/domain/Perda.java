package br.com.naepoca.merchant.domain;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.time.LocalDate;
@Entity @Table(name="perda")
public class Perda {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="comerciante_id", nullable=false) private Long comercianteId;
  @Column(name="produto_id", nullable=false) private Long produtoId;
  @Column(name="lote_envio_id") private Long loteEnvioId;
  @Column(nullable=false) private BigDecimal kg;
  @Column(nullable=false) private String motivo;
  @Column(name="valor_estimado_envio") private BigDecimal valorEstimadoEnvio;
  @Column(name="data_ref", nullable=false) private LocalDate dataRef;
  @Column(name="criado_em") private Instant criadoEm = Instant.now();
  public Long getId(){return id;} public Long getComercianteId(){return comercianteId;}
  public void setComercianteId(Long v){comercianteId=v;} public Long getProdutoId(){return produtoId;}
  public void setProdutoId(Long v){produtoId=v;} public Long getLoteEnvioId(){return loteEnvioId;}
  public void setLoteEnvioId(Long v){loteEnvioId=v;} public BigDecimal getKg(){return kg;}
  public void setKg(BigDecimal v){kg=v;} public String getMotivo(){return motivo;}
  public void setMotivo(String v){motivo=v;} public BigDecimal getValorEstimadoEnvio(){return valorEstimadoEnvio;}
  public void setValorEstimadoEnvio(BigDecimal v){valorEstimadoEnvio=v;} public LocalDate getDataRef(){return dataRef;}
  public void setDataRef(LocalDate v){dataRef=v;} public Instant getCriadoEm(){return criadoEm;}
}
