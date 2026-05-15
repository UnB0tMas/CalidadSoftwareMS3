package com.upsjb.ms3.security.principal;

import com.upsjb.ms3.security.jwt.JwtClaimNames;
import com.upsjb.ms3.security.roles.SecurityRoles;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CurrentUserResolver {

    private final String userIdClaim;
    private final String sessionIdClaim;
    private final String tokenTypeClaim;
    private final String roleClaim;
    private final String rolesClaim;
    private final String authoritiesClaim;

    public CurrentUserResolver(
            @Value("${ms1.claims.user-id:id_usuario_ms1}") String userIdClaim,
            @Value("${ms1.claims.session-id:sid}") String sessionIdClaim,
            @Value("${ms1.claims.token-type:typ}") String tokenTypeClaim,
            @Value("${ms1.claims.role:rol}") String roleClaim,
            @Value("${ms1.claims.roles:roles}") String rolesClaim,
            @Value("${ms1.claims.authorities:authorities}") String authoritiesClaim
    ) {
        this.userIdClaim = userIdClaim;
        this.sessionIdClaim = sessionIdClaim;
        this.tokenTypeClaim = tokenTypeClaim;
        this.roleClaim = roleClaim;
        this.rolesClaim = rolesClaim;
        this.authoritiesClaim = authoritiesClaim;
    }

    public AuthenticatedUserContext resolveRequired() {
        return resolveOptional()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException(
                        "No existe usuario autenticado para esta operación."
                ));
    }

    public Optional<AuthenticatedUserContext> resolveOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Jwt jwt = resolveJwt(authentication);

        if (jwt == null) {
            return Optional.empty();
        }

        Set<String> authorities = resolveAuthorities(authentication, jwt);
        String rolPrincipal = resolvePrincipalRole(authorities, jwt);

        AuthenticatedUserContext context = new AuthenticatedUserContext(
                claimAsLong(jwt, userIdClaim, JwtClaimNames.USER_ID, JwtClaimNames.USER_ID_ALT),
                claimAsLong(jwt, JwtClaimNames.EMPLOYEE_ID),
                firstTextClaim(jwt, JwtClaimNames.USERNAME, JwtClaimNames.PREFERRED_USERNAME, JwtClaimNames.NAME, JwtClaimNames.SUBJECT),
                firstTextClaim(jwt, JwtClaimNames.EMAIL),
                rolPrincipal,
                authorities,
                firstTextClaim(jwt, sessionIdClaim, JwtClaimNames.SESSION_ID, JwtClaimNames.SESSION_ID_ALT),
                firstTextClaim(jwt, tokenTypeClaim, JwtClaimNames.TOKEN_TYPE, JwtClaimNames.TOKEN_TYPE_ALT)
        );

        return Optional.of(context);
    }

    private Jwt resolveJwt(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getToken();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            return jwt;
        }

        return null;
    }

    private Set<String> resolveAuthorities(Authentication authentication, Jwt jwt) {
        Set<String> result = new LinkedHashSet<>();

        Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();

        if (grantedAuthorities != null) {
            grantedAuthorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(StringUtils::hasText)
                    .forEach(result::add);
        }

        addAuthoritiesFromClaim(jwt, roleClaim, result);
        addAuthoritiesFromClaim(jwt, rolesClaim, result);
        addAuthoritiesFromClaim(jwt, authoritiesClaim, result);

        return result;
    }

    private void addAuthoritiesFromClaim(Jwt jwt, String claimName, Set<String> result) {
        if (!StringUtils.hasText(claimName)) {
            return;
        }

        Object value = jwt.getClaims().get(claimName);

        if (value instanceof String stringValue) {
            addAuthorityFromRawValue(stringValue, result);
            return;
        }

        if (value instanceof Collection<?> collection) {
            collection.stream()
                    .filter(item -> item != null)
                    .map(String::valueOf)
                    .forEach(item -> addAuthorityFromRawValue(item, result));
        }
    }

    private void addAuthorityFromRawValue(String rawValue, Set<String> result) {
        if (!StringUtils.hasText(rawValue)) {
            return;
        }

        for (String candidate : rawValue.split("[,\\s]+")) {
            SecurityRoles.toAuthority(candidate).ifPresent(result::add);
        }
    }

    private String resolvePrincipalRole(Set<String> authorities, Jwt jwt) {
        if (SecurityRoles.isAdmin(authorities)) {
            return SecurityRoles.ADMIN;
        }

        if (SecurityRoles.isEmpleado(authorities)) {
            return SecurityRoles.EMPLEADO;
        }

        if (SecurityRoles.isCliente(authorities)) {
            return SecurityRoles.CLIENTE;
        }

        String rawRole = firstTextClaim(jwt, roleClaim, JwtClaimNames.ROL, JwtClaimNames.ROLE);
        String normalized = SecurityRoles.normalizeRoleName(rawRole);

        return SecurityRoles.isRecognizedRole(normalized) ? normalized : null;
    }

    private String firstTextClaim(Jwt jwt, String... claimNames) {
        if (claimNames == null) {
            return null;
        }

        for (String claimName : claimNames) {
            if (!StringUtils.hasText(claimName)) {
                continue;
            }

            Object value = jwt.getClaims().get(claimName);

            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }

        return null;
    }

    private Long claimAsLong(Jwt jwt, String... claimNames) {
        String value = firstTextClaim(jwt, claimNames);

        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new AuthenticationCredentialsNotFoundException(
                    "El claim de usuario autenticado no tiene formato numérico válido."
            );
        }
    }
}