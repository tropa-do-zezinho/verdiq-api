package br.com.naepoca.merchant.domain;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.time.LocalDate;
@Entity @Table(name="compra")
public class Compra {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="comerciante_id", nullable=false) private Long comercianteId;
  @Column(name="produto_id", nullable=false) private Long produtoId;
  @Column(name="lote_envio_id") private Long loteEnvioId;
  @Column(nullable=false) private BigDecimal kg;
  @Column(name="valor_total", nullable=false) private BigDecimal valorTotal;
  @Column(name="valor_unitario_envio", nullable=false) private BigDecimal valorUnitarioEnvio;
  @Column(name="data_ref", nullable=false) private LocalDate dataRef;
  private String fornecedor;
  @Column(name="criado_em") private Instant criadoEm = Instant.now();
  public Long getId(){return id;} public Long getComercianteId(){return comercianteId;}
  public void setComercianteId(Long v){comercianteId=v;} public Long getProdutoId(){return produtoId;}
  public void setProdutoId(Long v){produtoId=v;} public Long getLoteEnvioId(){return loteEnvioId;}
  public void setLoteEnvioId(Long v){loteEnvioId=v;} public BigDecimal getKg(){return kg;}
  public void setKg(BigDecimal v){kg=v;} public BigDecimal getValorTotal(){return valorTotal;}
  public void setValorTotal(BigDecimal v){valorTotal=v;} public BigDecimal getValorUnitarioEnvio(){return valorUnitarioEnvio;}
  public void setValorUnitarioEnvio(BigDecimal v){valorUnitarioEnvio=v;} public LocalDate getDataRef(){return dataRef;}
  public void setDataRef(LocalDate v){dataRef=v;} public String getFornecedor(){return fornecedor;}
  public void setFornecedor(String v){fornecedor=v;} public Instant getCriadoEm(){return criadoEm;}
}
