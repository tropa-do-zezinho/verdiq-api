package br.com.naepoca.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_token")
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "usuario_id", nullable = false) private Long usuarioId;
    @Column(name = "token_hash", nullable = false, unique = true) private String tokenHash;
    @Column(name = "expira_em", nullable = false) private Instant expiraEm;
    @Column(nullable = false) private Boolean revogado = false;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm = Instant.now();

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public Instant getExpiraEm() { return expiraEm; }
    public void setExpiraEm(Instant expiraEm) { this.expiraEm = expiraEm; }
    public Boolean getRevogado() { return revogado; }
    public void setRevogado(Boolean revogado) { this.revogado = revogado; }
}
