package br.com.naepoca.seasonality.domain;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;
@Entity @Table(name="indice_sazonal")
public class IndiceSazonal {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="produto_id", nullable=false) private Long produtoId;
  @Column(name="praca_id") private Long pracaId;
  @Column(name="comerciante_id") private Long comercianteId;
  @Column(nullable=false) private Integer mes;
  @Column(nullable=false) private String tipo;
  @Column(nullable=false) private BigDecimal valor;
  @Column(name="anos_usados", nullable=false) private Integer anosUsados;
  @Column(nullable=false) private String confianca;
  @Column(name="calculado_em") private Instant calculadoEm = Instant.now();
  public Long getId(){return id;} public Long getProdutoId(){return produtoId;} public void setProdutoId(Long v){produtoId=v;}
  public Long getPracaId(){return pracaId;} public void setPracaId(Long v){pracaId=v;}
  public Long getComercianteId(){return comercianteId;} public void setComercianteId(Long v){comercianteId=v;}
  public Integer getMes(){return mes;} public void setMes(Integer v){mes=v;}
  public String getTipo(){return tipo;} public void setTipo(String v){tipo=v;}
  public BigDecimal getValor(){return valor;} public void setValor(BigDecimal v){valor=v;}
  public Integer getAnosUsados(){return anosUsados;} public void setAnosUsados(Integer v){anosUsados=v;}
  public String getConfianca(){return confianca;} public void setConfianca(String v){confianca=v;}
  public Instant getCalculadoEm(){return calculadoEm;} public void setCalculadoEm(Instant v){calculadoEm=v;}
}
