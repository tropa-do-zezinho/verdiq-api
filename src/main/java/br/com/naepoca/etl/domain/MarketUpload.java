package br.com.naepoca.etl.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="market_upload")
public class MarketUpload {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="usuario_id", nullable=false) private Long usuarioId;
  @Column(nullable=false) private String tipo;
  @Column(name="arquivo_nome", nullable=false) private String arquivoNome;
  @Column(nullable=false) private String status = "VALIDADO";
  @Column(name="qtd_linhas", nullable=false) private Integer qtdLinhas = 0;
  private String mensagem; @Column(name="criado_em") private Instant criadoEm = Instant.now();
  public Long getId(){return id;} public Long getUsuarioId(){return usuarioId;} public void setUsuarioId(Long v){usuarioId=v;}
  public String getTipo(){return tipo;} public void setTipo(String v){tipo=v;}
  public String getArquivoNome(){return arquivoNome;} public void setArquivoNome(String v){arquivoNome=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
  public Integer getQtdLinhas(){return qtdLinhas;} public void setQtdLinhas(Integer v){qtdLinhas=v;}
  public String getMensagem(){return mensagem;} public void setMensagem(String v){mensagem=v;}
  public Instant getCriadoEm(){return criadoEm;}
}
