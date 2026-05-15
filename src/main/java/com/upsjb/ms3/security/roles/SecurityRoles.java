package com.upsjb.ms3.security.roles;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

public final class SecurityRoles {

    public static final String ADMIN = "ADMIN";
    public static final String EMPLEADO = "EMPLEADO";
    public static final String CLIENTE = "CLIENTE";

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_EMPLEADO = "ROLE_EMPLEADO";
    public static final String ROLE_CLIENTE = "ROLE_CLIENTE";

    private static final Collection<String> VALID_ROLES = Arrays.asList(ADMIN, EMPLEADO, CLIENTE);

    private SecurityRoles() {
    }

    public static Optional<String> toAuthority(String rawRoleOrAuthority) {
        String role = normalizeRoleName(rawRoleOrAuthority);

        if (!isRecognizedRole(role)) {
            return Optional.empty();
        }

        return Optional.of("ROLE_" + role);
    }

    public static String normalizeRoleName(String rawRoleOrAuthority) {
        if (rawRoleOrAuthority == null || rawRoleOrAuthority.isBlank()) {
            return "";
        }

        String value = rawRoleOrAuthority.trim().toUpperCase(Locale.ROOT);

        if (value.startsWith("ROLE_")) {
            value = value.substring("ROLE_".length());
        }

        if (value.startsWith("SCOPE_")) {
            value = value.substring("SCOPE_".length());
        }

        return value;
    }

    public static boolean isRecognizedRole(String normalizedRoleName) {
        if (normalizedRoleName == null || normalizedRoleName.isBlank()) {
            return false;
        }

        return VALID_ROLES.contains(normalizedRoleName.trim().toUpperCase(Locale.ROOT));
    }

    public static boolean isAdmin(Collection<String> authorities) {
        return hasAuthority(authorities, ROLE_ADMIN);
    }

    public static boolean isEmpleado(Collection<String> authorities) {
        return hasAuthority(authorities, ROLE_EMPLEADO);
    }

    public static boolean isCliente(Collection<String> authorities) {
        return hasAuthority(authorities, ROLE_CLIENTE);
    }

    public static boolean hasAuthority(Collection<String> authorities, String requiredAuthority) {
        if (authorities == null || requiredAuthority == null || requiredAuthority.isBlank()) {
            return false;
        }

        return authorities.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .anyMatch(value -> value.equals(requiredAuthority.trim().toUpperCase(Locale.ROOT)));
    }
}