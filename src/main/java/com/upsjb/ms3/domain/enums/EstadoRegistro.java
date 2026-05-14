package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum EstadoRegistro {

    ACTIVO("ACTIVO", "Activo", true),
    INACTIVO("INACTIVO", "Inactivo", false);

    private final String code;
    private final String label;
    private final boolean activo;

    EstadoRegistro(String code, String label, boolean activo) {
        this.code = code;
        this.label = label;
        this.activo = activo;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isActivo() {
        return activo;
    }

    public static EstadoRegistro fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El código de estado de registro es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de registro no válido: " + code));
    }
}