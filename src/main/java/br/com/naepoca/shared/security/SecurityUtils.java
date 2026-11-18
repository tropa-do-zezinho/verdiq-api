package br.com.naepoca.shared.security;

import br.com.naepoca.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static UserPrincipal currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Autenticação necessária");
        }
        return principal;
    }

    public static Long currentUserId() {
        return currentUser().id();
    }
}
