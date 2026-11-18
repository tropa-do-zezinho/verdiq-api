package br.com.naepoca.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "usuario_id", nullable = false) private Long usuarioId;
    @Column(name = "token_hash", nullable = false, unique = true) private String tokenHash;
    @Column(name = "expira_em", nullable = false) private Instant expiraEm;
    @Column(nullable = false) private Boolean usado = false;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm = Instant.now();

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public Instant getExpiraEm() { return expiraEm; }
    public void setExpiraEm(Instant expiraEm) { this.expiraEm = expiraEm; }
    public Boolean getUsado() { return usado; }
    public void setUsado(Boolean usado) { this.usado = usado; }
}
