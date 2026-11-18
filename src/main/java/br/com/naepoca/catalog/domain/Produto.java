package br.com.naepoca.catalog.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="produto")
public class Produto {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="nome_canonico", nullable=false, unique=true) private String nomeCanonico;
  @Column(nullable=false) private String categoria;
  @Column(name="unidade_base", nullable=false) private String unidadeBase = "KG";
  private Boolean perecivel = true;
  private Boolean ativo = true;
  @Column(name="criado_em") private Instant criadoEm = Instant.now();
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public String getNomeCanonico(){return nomeCanonico;} public void setNomeCanonico(String v){nomeCanonico=v;}
  public String getCategoria(){return categoria;} public void setCategoria(String v){categoria=v;}
  public String getUnidadeBase(){return unidadeBase;} public void setUnidadeBase(String v){unidadeBase=v;}
  public Boolean getPerecivel(){return perecivel;} public Boolean getAtivo(){return ativo;}
  public void setAtivo(Boolean ativo){this.ativo=ativo;}
}
