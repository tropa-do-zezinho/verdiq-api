package br.com.naepoca.market.domain;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;
@Entity @Table(name="producao")
public class Producao {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="produto_id", nullable=false) private Long produtoId;
  @Column(nullable=false, length=2) private String uf;
  private String municipio; @Column(nullable=false) private Integer ano;
  @Column(name="area_ha") private BigDecimal areaHa; @Column(name="producao_t") private BigDecimal producaoT;
  private BigDecimal rendimento; @Column(name="valor_rs") private BigDecimal valorRs;
  @Column(nullable=false) private String fonte = "IBGE_PAM";
  @Column(name="coletado_em") private Instant coletadoEm = Instant.now();
  public Long getId(){return id;} public Long getProdutoId(){return produtoId;} public void setProdutoId(Long v){produtoId=v;}
  public String getUf(){return uf;} public void setUf(String v){uf=v;} public String getMunicipio(){return municipio;}
  public void setMunicipio(String v){municipio=v;} public Integer getAno(){return ano;} public void setAno(Integer v){ano=v;}
  public BigDecimal getAreaHa(){return areaHa;} public void setAreaHa(BigDecimal v){areaHa=v;}
  public BigDecimal getProducaoT(){return producaoT;} public void setProducaoT(BigDecimal v){producaoT=v;}
  public BigDecimal getRendimento(){return rendimento;} public void setRendimento(BigDecimal v){rendimento=v;}
  public BigDecimal getValorRs(){return valorRs;} public void setValorRs(BigDecimal v){valorRs=v;}
  public String getFonte(){return fonte;} public void setFonte(String v){fonte=v;}
}
