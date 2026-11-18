package br.com.naepoca.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank String nome,
            @Email String email,
            String telefone,
            @NotBlank @Size(min = 8) String senha,
            @NotNull String papel,
            String cidade,
            String uf,
            Boolean consentimentoNotificacoes,
            Boolean consentimentoComparativos,
            MerchantProfile merchant,
            ClientProfile client
    ) {}

    public record MerchantProfile(
            @NotBlank String nomeFantasia,
            @NotBlank String tipoNegocio,
            Long pracaId,
            List<Long> produtosPrincipais
    ) {}

    public record ClientProfile(
            String cidade,
            String uf,
            List<Long> produtosInteresse
    ) {}

    public record LoginRequest(
            @NotBlank String login,
            @NotBlank String senha
    ) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record PasswordResetRequest(@NotBlank String email) {}

    public record PasswordResetConfirm(
            @NotBlank String token,
            @NotBlank @Size(min = 8) String novaSenha
    ) {}

    public record TokenResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresInMinutes,
            UserResponse user
    ) {}

    public record UserResponse(
            Long id,
            String nome,
            String email,
            String telefone,
            String papel,
            String cidade,
            String uf,
            Boolean consentimentoNotificacoes,
            Boolean consentimentoComparativos
    ) {}

    public record UpdateProfileRequest(
            String nome,
            String cidade,
            String uf,
            Boolean consentimentoNotificacoes,
            Boolean consentimentoComparativos,
            MerchantProfile merchant,
            ClientProfile client
    ) {}
}
