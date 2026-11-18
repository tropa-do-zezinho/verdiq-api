package br.com.naepoca.merchant.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="lote_envio")
public class LoteEnvio {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="comerciante_id", nullable=false) private Long comercianteId;
  @Column(nullable=false) private String tipo;
  @Column(nullable=false) private String origem = "MANUAL";
  @Column(name="arquivo_nome") private String arquivoNome;
  @Column(name="qtd_linhas", nullable=false) private Integer qtdLinhas = 0;
  @Column(name="criado_em") private Instant criadoEm = Instant.now();
  public Long getId(){return id;} public Long getComercianteId(){return comercianteId;}
  public void setComercianteId(Long v){comercianteId=v;} public String getTipo(){return tipo;}
  public void setTipo(String v){tipo=v;} public String getOrigem(){return origem;}
  public void setOrigem(String v){origem=v;} public String getArquivoNome(){return arquivoNome;}
  public void setArquivoNome(String v){arquivoNome=v;} public Integer getQtdLinhas(){return qtdLinhas;}
  public void setQtdLinhas(Integer v){qtdLinhas=v;} public Instant getCriadoEm(){return criadoEm;}
}
