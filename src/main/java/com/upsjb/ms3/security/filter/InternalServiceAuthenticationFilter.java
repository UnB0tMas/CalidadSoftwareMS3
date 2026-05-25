// ruta: src/main/java/com/upsjb/ms3/security/filter/InternalServiceAuthenticationFilter.java
package com.upsjb.ms3.security.filter;

import com.upsjb.ms3.config.InternalSecurityProperties;
import com.upsjb.ms3.security.handler.SecurityExceptionHandler;
import com.upsjb.ms3.security.roles.SecurityRoles;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {

    private static final String INTERNAL_API_PREFIX = "/api/internal/";

    private final InternalSecurityProperties properties;
    private final SecurityExceptionHandler securityExceptionHandler;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith(INTERNAL_API_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!properties.isEnabled()) {
            securityExceptionHandler.writeUnauthorized(
                    request,
                    response,
                    "La autenticación interna del MS3 está deshabilitada."
            );
            return;
        }

        try {
            validateInternalCredential(request);
            authenticateInternalService(request);
            filterChain.doFilter(request, response);
        } catch (AuthenticationCredentialsNotFoundException ex) {
            SecurityContextHolder.clearContext();
            securityExceptionHandler.writeUnauthorized(
                    request,
                    response,
                    "Credencial interna ausente o inválida."
            );
        }
    }

    private void validateInternalCredential(HttpServletRequest request) {
        String headerName = properties.getHeaderName();
        String expectedKey = properties.getServiceKey();

        if (!StringUtils.hasText(headerName) || !StringUtils.hasText(expectedKey)) {
            throw new AuthenticationCredentialsNotFoundException(
                    "La seguridad interna no está correctamente configurada."
            );
        }

        String providedKey = request.getHeader(headerName);

        if (!StringUtils.hasText(providedKey)) {
            throw new AuthenticationCredentialsNotFoundException(
                    "No se recibió la credencial interna."
            );
        }

        if (!constantTimeEquals(expectedKey.trim(), providedKey.trim())) {
            throw new AuthenticationCredentialsNotFoundException(
                    "La credencial interna no coincide."
            );
        }
    }

    private void authenticateInternalService(HttpServletRequest request) {
        PreAuthenticatedAuthenticationToken authentication =
                new PreAuthenticatedAuthenticationToken(
                        SecurityRoles.INTERNAL_SERVICE,
                        "N/A",
                        List.of(new SimpleGrantedAuthority(SecurityRoles.ROLE_INTERNAL_SERVICE))
                );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean constantTimeEquals(String expected, String provided) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] providedBytes = provided.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(expectedBytes, providedBytes);
    }
}