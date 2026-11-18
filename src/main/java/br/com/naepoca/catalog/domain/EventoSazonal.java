package br.com.naepoca.catalog.domain;
import jakarta.persistence.*;
@Entity @Table(name="evento_sazonal")
public class EventoSazonal {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false) private String nome;
  @Column(name="regra_data", nullable=false) private String regraData;
  @Column(name="mes_tipico") private Integer mesTipico;
  private String descricao;
  public Long getId(){return id;} public String getNome(){return nome;}
  public String getRegraData(){return regraData;} public Integer getMesTipico(){return mesTipico;}
  public String getDescricao(){return descricao;}
  public void setNome(String v){nome=v;} public void setRegraData(String v){regraData=v;}
  public void setMesTipico(Integer v){mesTipico=v;} public void setDescricao(String v){descricao=v;}
}
