package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum RolSistema {

    ADMIN("ADMIN", "Administrador"),
    EMPLEADO("EMPLEADO", "Empleado"),
    CLIENTE("CLIENTE", "Cliente"),
    ANONIMO("ANONIMO", "Anónimo"),
    SISTEMA("SISTEMA", "Sistema");

    private final String code;
    private final String label;

    RolSistema(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getAuthority() {
        return "ROLE_" + code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isEmpleado() {
        return this == EMPLEADO;
    }

    public boolean isCliente() {
        return this == CLIENTE;
    }

    public boolean isAnonimo() {
        return this == ANONIMO;
    }

    public boolean isSistema() {
        return this == SISTEMA;
    }

    public static RolSistema fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El rol del sistema es obligatorio.");
        }

        String normalized = code.trim();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }

        final String finalCode = normalized;

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(finalCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rol del sistema no válido: " + code));
    }
}