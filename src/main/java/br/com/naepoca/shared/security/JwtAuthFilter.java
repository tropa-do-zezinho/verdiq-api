package br.com.naepoca.shared.security;

import br.com.naepoca.auth.repo.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Long userId = jwtService.userId(token);
                usuarioRepository.findById(userId).ifPresent(user -> {
                    if (Boolean.TRUE.equals(user.getAtivo()) && user.getExcluidoEm() == null) {
                        UserPrincipal principal = new UserPrincipal(
                                user.getId(), user.getEmail(), user.getSenhaHash(),
                                Role.valueOf(user.getPapel()), true);
                        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                });
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
