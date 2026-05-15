package com.upsjb.ms3.security.principal;

import com.upsjb.ms3.security.roles.SecurityRoles;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;

@Getter
public final class AuthenticatedUserContext {

    private final Long idUsuarioMs1;
    private final Long idEmpleadoMs2;
    private final String username;
    private final String email;
    private final String rolPrincipal;
    private final Set<String> authorities;
    private final String sessionId;
    private final String tokenType;

    public AuthenticatedUserContext(
            Long idUsuarioMs1,
            Long idEmpleadoMs2,
            String username,
            String email,
            String rolPrincipal,
            Collection<String> authorities,
            String sessionId,
            String tokenType
    ) {
        this.idUsuarioMs1 = idUsuarioMs1;
        this.idEmpleadoMs2 = idEmpleadoMs2;
        this.username = username;
        this.email = email;
        this.rolPrincipal = rolPrincipal;
        this.authorities = Collections.unmodifiableSet(new LinkedHashSet<>(authorities == null ? Set.of() : authorities));
        this.sessionId = sessionId;
        this.tokenType = tokenType;
    }

    public boolean isAdmin() {
        return SecurityRoles.isAdmin(authorities);
    }

    public boolean isEmpleado() {
        return SecurityRoles.isEmpleado(authorities);
    }

    public boolean isCliente() {
        return SecurityRoles.isCliente(authorities);
    }

    public boolean hasAuthority(String authority) {
        return SecurityRoles.hasAuthority(authorities, authority);
    }

    public boolean hasAnyAuthority(String... requiredAuthorities) {
        if (requiredAuthorities == null || requiredAuthorities.length == 0) {
            return false;
        }

        for (String requiredAuthority : requiredAuthorities) {
            if (hasAuthority(requiredAuthority)) {
                return true;
            }
        }

        return false;
    }

    public String actorLabel() {
        if (username != null && !username.isBlank()) {
            return username;
        }

        if (email != null && !email.isBlank()) {
            return email;
        }

        return idUsuarioMs1 == null ? "UNKNOWN" : "USUARIO_MS1_" + idUsuarioMs1;
    }
}