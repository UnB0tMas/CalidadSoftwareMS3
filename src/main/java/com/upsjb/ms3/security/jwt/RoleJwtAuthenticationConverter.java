package com.upsjb.ms3.security.jwt;

import com.upsjb.ms3.security.roles.SecurityRoles;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RoleJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    private final String roleClaim;
    private final String rolesClaim;
    private final String authoritiesClaim;

    public RoleJwtAuthenticationConverter(
            @Value("${ms1.claims.role:rol}") String roleClaim,
            @Value("${ms1.claims.roles:roles}") String rolesClaim,
            @Value("${ms1.claims.authorities:authorities}") String authoritiesClaim
    ) {
        this.roleClaim = roleClaim;
        this.rolesClaim = rolesClaim;
        this.authoritiesClaim = authoritiesClaim;
    }

    @Override
    public JwtAuthenticationToken convert(@NonNull Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        addAuthoritiesFromClaim(jwt, roleClaim, authorities);
        addAuthoritiesFromClaim(jwt, rolesClaim, authorities);
        addAuthoritiesFromClaim(jwt, authoritiesClaim, authorities);

        addAuthoritiesFromClaim(jwt, JwtClaimNames.ROL, authorities);
        addAuthoritiesFromClaim(jwt, JwtClaimNames.ROLE, authorities);
        addAuthoritiesFromClaim(jwt, JwtClaimNames.ROLES, authorities);
        addAuthoritiesFromClaim(jwt, JwtClaimNames.AUTHORITIES, authorities);

        return new JwtAuthenticationToken(jwt, authorities, resolvePrincipalName(jwt));
    }

    private void addAuthoritiesFromClaim(Jwt jwt, String claimName, Set<GrantedAuthority> authorities) {
        if (!StringUtils.hasText(claimName)) {
            return;
        }

        Object claimValue = jwt.getClaims().get(claimName);

        if (claimValue == null) {
            return;
        }

        if (claimValue instanceof String value) {
            addAuthoritiesFromString(value, authorities);
            return;
        }

        if (claimValue instanceof Collection<?> collection) {
            collection.stream()
                    .filter(value -> value != null)
                    .map(String::valueOf)
                    .forEach(value -> addAuthoritiesFromString(value, authorities));
        }
    }

    private void addAuthoritiesFromString(String rawValue, Set<GrantedAuthority> authorities) {
        if (!StringUtils.hasText(rawValue)) {
            return;
        }

        List<String> candidates = List.of(rawValue.split("[,\\s]+"));

        for (String candidate : candidates) {
            SecurityRoles.toAuthority(candidate)
                    .map(SimpleGrantedAuthority::new)
                    .ifPresent(authorities::add);
        }
    }

    private String resolvePrincipalName(Jwt jwt) {
        String username = claimAsString(jwt, JwtClaimNames.USERNAME);
        if (StringUtils.hasText(username)) {
            return username;
        }

        String preferredUsername = claimAsString(jwt, JwtClaimNames.PREFERRED_USERNAME);
        if (StringUtils.hasText(preferredUsername)) {
            return preferredUsername;
        }

        String email = claimAsString(jwt, JwtClaimNames.EMAIL);
        if (StringUtils.hasText(email)) {
            return email;
        }

        return jwt.getSubject();
    }

    private String claimAsString(Jwt jwt, String claimName) {
        Object value = jwt.getClaims().get(claimName);
        return value == null ? null : String.valueOf(value);
    }
}