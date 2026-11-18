package br.com.naepoca.catalog.domain;
import jakarta.persistence.*;
@Entity @Table(name="praca")
public class Praca {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true) private String codigo;
  @Column(nullable=false) private String nome;
  @Column(nullable=false) private String cidade;
  @Column(nullable=false, length=2) private String uf;
  @Column(nullable=false) private String fonte = "PROHORT";
  public Long getId(){return id;} public String getCodigo(){return codigo;}
  public String getNome(){return nome;} public String getCidade(){return cidade;}
  public String getUf(){return uf;} public String getFonte(){return fonte;}
  public void setCodigo(String v){codigo=v;} public void setNome(String v){nome=v;}
  public void setCidade(String v){cidade=v;} public void setUf(String v){uf=v;}
}
