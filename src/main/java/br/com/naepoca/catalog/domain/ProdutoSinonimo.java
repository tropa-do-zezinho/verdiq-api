package br.com.naepoca.catalog.domain;
import jakarta.persistence.*;
@Entity @Table(name="produto_sinonimo")
public class ProdutoSinonimo {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="produto_id", nullable=false) private Long produtoId;
  @Column(nullable=false) private String sinonimo;
  public Long getId(){return id;} public Long getProdutoId(){return produtoId;}
  public void setProdutoId(Long v){produtoId=v;} public String getSinonimo(){return sinonimo;}
  public void setSinonimo(String v){sinonimo=v;}
}
