package br.com.naepoca.plans.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="plano")
public class Plano {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true) private String codigo;
  @Column(nullable=false) private String nome;
  @Column(name="preco_mensal", nullable=false) private BigDecimal precoMensal;
  @Column(nullable=false) private String beneficios;
  @Column(nullable=false) private Boolean ativo = true;
  public Long getId(){return id;} public String getCodigo(){return codigo;} public String getNome(){return nome;}
  public BigDecimal getPrecoMensal(){return precoMensal;} public String getBeneficios(){return beneficios;}
  public Boolean getAtivo(){return ativo;}
}
