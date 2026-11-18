package br.com.naepoca.etl.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="etl_run")
public class EtlRun {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false) private String fonte; @Column(nullable=false) private String status;
  @Column(name="iniciado_em") private Instant iniciadoEm = Instant.now();
  @Column(name="finalizado_em") private Instant finalizadoEm;
  private Integer registros = 0; private String mensagem;
  @Column(name="autor_usuario_id") private Long autorUsuarioId;
  public Long getId(){return id;} public String getFonte(){return fonte;} public void setFonte(String v){fonte=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
  public Instant getIniciadoEm(){return iniciadoEm;} public Instant getFinalizadoEm(){return finalizadoEm;}
  public void setFinalizadoEm(Instant v){finalizadoEm=v;} public Integer getRegistros(){return registros;}
  public void setRegistros(Integer v){registros=v;} public String getMensagem(){return mensagem;}
  public void setMensagem(String v){mensagem=v;} public Long getAutorUsuarioId(){return autorUsuarioId;}
  public void setAutorUsuarioId(Long v){autorUsuarioId=v;}
}
