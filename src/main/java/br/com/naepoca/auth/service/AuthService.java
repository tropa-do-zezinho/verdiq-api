package br.com.naepoca.auth.service;

import br.com.naepoca.auth.domain.PasswordResetToken;
import br.com.naepoca.auth.domain.RefreshToken;
import br.com.naepoca.auth.domain.Usuario;
import br.com.naepoca.auth.dto.AuthDtos;
import br.com.naepoca.auth.repo.PasswordResetTokenRepository;
import br.com.naepoca.auth.repo.RefreshTokenRepository;
import br.com.naepoca.auth.repo.UsuarioRepository;
import br.com.naepoca.config.NaEpocaProperties;
import br.com.naepoca.merchant.domain.ClientePerfil;
import br.com.naepoca.merchant.domain.Comerciante;
import br.com.naepoca.merchant.repo.ClientePerfilRepository;
import br.com.naepoca.merchant.repo.ComercianteRepository;
import br.com.naepoca.notifications.domain.PreferenciaNotificacao;
import br.com.naepoca.notifications.repo.PreferenciaNotificacaoRepository;
import br.com.naepoca.shared.exception.ApiException;
import br.com.naepoca.shared.security.JwtService;
import br.com.naepoca.shared.security.Role;
import br.com.naepoca.shared.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ComercianteRepository comercianteRepository;
    private final ClientePerfilRepository clientePerfilRepository;
    private final PreferenciaNotificacaoRepository preferenciaNotificacaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NaEpocaProperties properties;
    private final JdbcTemplate jdbcTemplate;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            ComercianteRepository comercianteRepository,
            ClientePerfilRepository clientePerfilRepository,
            PreferenciaNotificacaoRepository preferenciaNotificacaoRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            NaEpocaProperties properties,
            JdbcTemplate jdbcTemplate
    ) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.comercianteRepository = comercianteRepository;
        this.clientePerfilRepository = clientePerfilRepository;
        this.preferenciaNotificacaoRepository = preferenciaNotificacaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.properties = properties;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public AuthDtos.TokenResponse register(AuthDtos.RegisterRequest req) {
        Role role;
        try {
            role = Role.valueOf(req.papel().toUpperCase());
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ROLE", "Papel inválido");
        }
        if (role == Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ADMIN_FORBIDDEN", "Cadastro de admin não permitido");
        }
        if (req.email() == null && (req.telefone() == null || req.telefone().isBlank())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LOGIN_REQUIRED", "Informe e-mail ou telefone");
        }
        if (req.email() != null && usuarioRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "E-mail já cadastrado");
        }

        Usuario user = new Usuario();
        user.setNome(req.nome());
        user.setEmail(req.email() == null ? null : req.email().trim().toLowerCase());
        user.setTelefone(req.telefone());
        user.setSenhaHash(passwordEncoder.encode(req.senha()));
        user.setPapel(role.name());
        user.setCidade(req.cidade());
        user.setUf(req.uf() == null ? "SP" : req.uf());
        user.setConsentimentoNotificacoes(Boolean.TRUE.equals(req.consentimentoNotificacoes()));
        user.setConsentimentoComparativos(Boolean.TRUE.equals(req.consentimentoComparativos()));
        user = usuarioRepository.save(user);

        PreferenciaNotificacao pref = new PreferenciaNotificacao();
        pref.setUsuarioId(user.getId());
        preferenciaNotificacaoRepository.save(pref);

        if (role == Role.COMERCIANTE) {
            if (req.merchant() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "MERCHANT_REQUIRED", "Perfil de comerciante obrigatório");
            }
            Comerciante c = new Comerciante();
            c.setUsuarioId(user.getId());
            c.setNomeFantasia(req.merchant().nomeFantasia());
            c.setTipoNegocio(req.merchant().tipoNegocio());
            c.setPracaId(req.merchant().pracaId() == null ? 1L : req.merchant().pracaId());
            c.setPlano("ESSENCIAL");
            c = comercianteRepository.save(c);
            if (req.merchant().produtosPrincipais() != null) {
                for (Long pid : req.merchant().produtosPrincipais()) {
                    jdbcTemplate.update(
                            "INSERT INTO comerciante_produto_principal(comerciante_id, produto_id) VALUES (?,?) ON CONFLICT DO NOTHING",
                            c.getId(), pid);
                }
            }
        } else if (role == Role.CLIENTE) {
            ClientePerfil perfil = new ClientePerfil();
            perfil.setUsuarioId(user.getId());
            perfil.setCidade(req.client() != null && req.client().cidade() != null ? req.client().cidade() : req.cidade());
            perfil.setUf(req.client() != null && req.client().uf() != null ? req.client().uf() : user.getUf());
            perfil = clientePerfilRepository.save(perfil);
            if (req.client() != null && req.client().produtosInteresse() != null) {
                for (Long pid : req.client().produtosInteresse()) {
                    jdbcTemplate.update(
                            "INSERT INTO cliente_interesse(cliente_perfil_id, produto_id) VALUES (?,?) ON CONFLICT DO NOTHING",
                            perfil.getId(), pid);
                }
            }
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest req) {
        Usuario user = usuarioRepository.findByEmailIgnoreCase(req.login().trim())
                .or(() -> usuarioRepository.findByTelefone(req.login().trim()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_INVALID", "Credenciais inválidas"));
        if (!Boolean.TRUE.equals(user.getAtivo()) || user.getExcluidoEm() != null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INACTIVE", "Conta inativa");
        }
        if (!passwordEncoder.matches(req.senha(), user.getSenhaHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_INVALID", "Credenciais inválidas");
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthDtos.TokenResponse refresh(AuthDtos.RefreshRequest req) {
        String hash = sha256(req.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevogadoFalse(hash)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_INVALID", "Refresh inválido"));
        if (stored.getExpiraEm().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_EXPIRED", "Refresh expirado");
        }
        stored.setRevogado(true);
        Usuario user = usuarioRepository.findById(stored.getUsuarioId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Usuário não encontrado"));
        return issueTokens(user);
    }

    @Transactional
    public String requestPasswordReset(AuthDtos.PasswordResetRequest req) {
        Usuario user = usuarioRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuário não encontrado"));
        String raw = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setUsuarioId(user.getId());
        token.setTokenHash(sha256(raw));
        token.setExpiraEm(Instant.now().plusSeconds(3600));
        passwordResetTokenRepository.save(token);
        // Sem e-mail: devolve token para teste manual (Postman)
        return raw;
    }

    @Transactional
    public void confirmPasswordReset(AuthDtos.PasswordResetConfirm req) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHashAndUsadoFalse(sha256(req.token()))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "TOKEN_INVALID", "Token inválido"));
        if (token.getExpiraEm().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "TOKEN_EXPIRED", "Token expirado");
        }
        Usuario user = usuarioRepository.findById(token.getUsuarioId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuário não encontrado"));
        user.setSenhaHash(passwordEncoder.encode(req.novaSenha()));
        user.setAtualizadoEm(Instant.now());
        token.setUsado(true);
    }

    @Transactional(readOnly = true)
    public AuthDtos.UserResponse me(Long userId) {
        return toUser(usuarioRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuário não encontrado")));
    }

    @Transactional
    public AuthDtos.UserResponse updateMe(Long userId, AuthDtos.UpdateProfileRequest req) {
        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuário não encontrado"));
        if (req.nome() != null) user.setNome(req.nome());
        if (req.cidade() != null) user.setCidade(req.cidade());
        if (req.uf() != null) user.setUf(req.uf());
        if (req.consentimentoNotificacoes() != null) user.setConsentimentoNotificacoes(req.consentimentoNotificacoes());
        if (req.consentimentoComparativos() != null) user.setConsentimentoComparativos(req.consentimentoComparativos());
        user.setAtualizadoEm(Instant.now());

        if (Role.COMERCIANTE.name().equals(user.getPapel()) && req.merchant() != null) {
            Comerciante c = comercianteRepository.findByUsuarioId(userId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MERCHANT_NOT_FOUND", "Comerciante não encontrado"));
            if (req.merchant().nomeFantasia() != null) c.setNomeFantasia(req.merchant().nomeFantasia());
            if (req.merchant().tipoNegocio() != null) c.setTipoNegocio(req.merchant().tipoNegocio());
            if (req.merchant().pracaId() != null) c.setPracaId(req.merchant().pracaId());
        }
        if (Role.CLIENTE.name().equals(user.getPapel()) && req.client() != null) {
            ClientePerfil p = clientePerfilRepository.findByUsuarioId(userId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CLIENT_NOT_FOUND", "Cliente não encontrado"));
            if (req.client().cidade() != null) p.setCidade(req.client().cidade());
            if (req.client().uf() != null) p.setUf(req.client().uf());
        }
        return toUser(user);
    }

    @Transactional
    public void deleteAccount(Long userId) {
        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuário não encontrado"));
        user.setAtivo(false);
        user.setExcluidoEm(Instant.now());
        user.setEmail(user.getEmail() == null ? null : "deleted+" + userId + "@naepoca.local");
        user.setTelefone(null);
        user.setAtualizadoEm(Instant.now());
    }

    private AuthDtos.TokenResponse issueTokens(Usuario user) {
        UserPrincipal principal = new UserPrincipal(
                user.getId(), user.getEmail(), user.getSenhaHash(), Role.valueOf(user.getPapel()), true);
        String access = jwtService.createAccessToken(principal);
        String refreshRaw = UUID.randomUUID().toString() + UUID.randomUUID();
        RefreshToken refresh = new RefreshToken();
        refresh.setUsuarioId(user.getId());
        refresh.setTokenHash(sha256(refreshRaw));
        refresh.setExpiraEm(Instant.now().plusSeconds(properties.jwt().refreshTokenDays() * 86400));
        refreshTokenRepository.save(refresh);
        return new AuthDtos.TokenResponse(
                access, refreshRaw, "Bearer", properties.jwt().accessTokenMinutes(), toUser(user));
    }

    private AuthDtos.UserResponse toUser(Usuario u) {
        return new AuthDtos.UserResponse(
                u.getId(), u.getNome(), u.getEmail(), u.getTelefone(), u.getPapel(),
                u.getCidade(), u.getUf(), u.getConsentimentoNotificacoes(), u.getConsentimentoComparativos());
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
