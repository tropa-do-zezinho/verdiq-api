package br.com.naepoca.offers.domain;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.time.LocalDate;
@Entity @Table(name="oferta")
public class Oferta {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="comerciante_id", nullable=false) private Long comercianteId;
  @Column(name="produto_id", nullable=false) private Long produtoId;
  @Column(name="preco_normal", nullable=false) private BigDecimal precoNormal;
  @Column(name="preco_promocional", nullable=false) private BigDecimal precoPromocional;
  @Column(name="quantidade_kg") private BigDecimal quantidadeKg;
  @Column(nullable=false) private LocalDate inicio; @Column(nullable=false) private LocalDate fim;
  @Column(nullable=false) private String status = "ATIVA";
  @Column(name="ultima_chance", nullable=false) private Boolean ultimaChance = false;
  @Column(name="lote_info") private String loteInfo;
  @Column(name="criado_em") private Instant criadoEm = Instant.now();
  public Long getId(){return id;} public Long getComercianteId(){return comercianteId;} public void setComercianteId(Long v){comercianteId=v;}
  public Long getProdutoId(){return produtoId;} public void setProdutoId(Long v){produtoId=v;}
  public BigDecimal getPrecoNormal(){return precoNormal;} public void setPrecoNormal(BigDecimal v){precoNormal=v;}
  public BigDecimal getPrecoPromocional(){return precoPromocional;} public void setPrecoPromocional(BigDecimal v){precoPromocional=v;}
  public BigDecimal getQuantidadeKg(){return quantidadeKg;} public void setQuantidadeKg(BigDecimal v){quantidadeKg=v;}
  public LocalDate getInicio(){return inicio;} public void setInicio(LocalDate v){inicio=v;}
  public LocalDate getFim(){return fim;} public void setFim(LocalDate v){fim=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
  public Boolean getUltimaChance(){return ultimaChance;} public void setUltimaChance(Boolean v){ultimaChance=v;}
  public String getLoteInfo(){return loteInfo;} public void setLoteInfo(String v){loteInfo=v;}
  public Instant getCriadoEm(){return criadoEm;}
}
