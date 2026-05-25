// ruta: src/main/java/com/upsjb/ms3/domain/enums/RolSistema.java
package com.upsjb.ms3.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

    @JsonValue
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

    @JsonCreator
    public static RolSistema fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El rol del sistema es obligatorio.");
        }

        String normalized = code.trim();

        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }

        String finalCode = normalized;

        return Arrays.stream(values())
                .filter(value ->
                        value.name().equalsIgnoreCase(finalCode)
                                || value.code.equalsIgnoreCase(finalCode)
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rol del sistema no válido: " + code));
    }
}