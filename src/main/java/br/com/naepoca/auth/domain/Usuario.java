package br.com.naepoca.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120) private String nome;
    @Column(unique = true, length = 180) private String email;
    @Column(unique = true, length = 20) private String telefone;
    @Column(name = "senha_hash", nullable = false) private String senhaHash;
    @Column(nullable = false, length = 20) private String papel;
    private String cidade;
    @Column(length = 2) private String uf = "SP";
    @Column(name = "consentimento_notificacoes", nullable = false) private Boolean consentimentoNotificacoes = false;
    @Column(name = "consentimento_comparativos", nullable = false) private Boolean consentimentoComparativos = false;
    @Column(nullable = false) private Boolean ativo = true;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm = Instant.now();
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm = Instant.now();
    @Column(name = "excluido_em") private Instant excluidoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public String getPapel() { return papel; }
    public void setPapel(String papel) { this.papel = papel; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public Boolean getConsentimentoNotificacoes() { return consentimentoNotificacoes; }
    public void setConsentimentoNotificacoes(Boolean v) { this.consentimentoNotificacoes = v; }
    public Boolean getConsentimentoComparativos() { return consentimentoComparativos; }
    public void setConsentimentoComparativos(Boolean v) { this.consentimentoComparativos = v; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public Instant getExcluidoEm() { return excluidoEm; }
    public void setExcluidoEm(Instant excluidoEm) { this.excluidoEm = excluidoEm; }
}
