package br.com.naepoca.seasonality.domain;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode; import org.hibernate.type.SqlTypes;
import java.time.Instant; import java.time.LocalDate; import java.util.Map;
@Entity @Table(name="recomendacao")
public class Recomendacao {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="comerciante_id") private Long comercianteId;
  @Column(name="produto_id", nullable=false) private Long produtoId;
  @Column(nullable=false) private String tipo; @Column(nullable=false) private String titulo;
  @Column(nullable=false) private String mensagem;
  @Column(nullable=false) private String confianca;
  @JdbcTypeCode(SqlTypes.JSON) @Column(name="dados_json", nullable=false)
  private Map<String, Object> dadosJson;
  @Column(name="periodo_inicio") private LocalDate periodoInicio;
  @Column(name="periodo_fim") private LocalDate periodoFim;
  @Column(nullable=false) private String fonte;
  @Column(name="gerado_em") private Instant geradoEm = Instant.now();
  public Long getId(){return id;} public Long getComercianteId(){return comercianteId;} public void setComercianteId(Long v){comercianteId=v;}
  public Long getProdutoId(){return produtoId;} public void setProdutoId(Long v){produtoId=v;}
  public String getTipo(){return tipo;} public void setTipo(String v){tipo=v;}
  public String getTitulo(){return titulo;} public void setTitulo(String v){titulo=v;}
  public String getMensagem(){return mensagem;} public void setMensagem(String v){mensagem=v;}
  public String getConfianca(){return confianca;} public void setConfianca(String v){confianca=v;}
  public Map<String,Object> getDadosJson(){return dadosJson;} public void setDadosJson(Map<String,Object> v){dadosJson=v;}
  public LocalDate getPeriodoInicio(){return periodoInicio;} public void setPeriodoInicio(LocalDate v){periodoInicio=v;}
  public LocalDate getPeriodoFim(){return periodoFim;} public void setPeriodoFim(LocalDate v){periodoFim=v;}
  public String getFonte(){return fonte;} public void setFonte(String v){fonte=v;}
  public Instant getGeradoEm(){return geradoEm;}
}
