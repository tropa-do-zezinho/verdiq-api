package br.com.naepoca.auth.web;

import br.com.naepoca.auth.dto.AuthDtos;
import br.com.naepoca.auth.service.AuthService;
import br.com.naepoca.shared.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.TokenResponse register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthDtos.TokenResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/password-reset/request")
    @Operation(summary = "Solicita reset; retorna token em texto (sem e-mail no MVP desta entrega)")
    public Map<String, String> requestReset(@Valid @RequestBody AuthDtos.PasswordResetRequest request) {
        return Map.of("token", authService.requestPasswordReset(request));
    }

    @PostMapping("/password-reset/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmReset(@Valid @RequestBody AuthDtos.PasswordResetConfirm request) {
        authService.confirmPasswordReset(request);
    }

    @GetMapping("/me")
    public AuthDtos.UserResponse me() {
        return authService.me(SecurityUtils.currentUserId());
    }

    @PutMapping("/me")
    public AuthDtos.UserResponse updateMe(@Valid @RequestBody AuthDtos.UpdateProfileRequest request) {
        return authService.updateMe(SecurityUtils.currentUserId(), request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe() {
        authService.deleteAccount(SecurityUtils.currentUserId());
    }
}
