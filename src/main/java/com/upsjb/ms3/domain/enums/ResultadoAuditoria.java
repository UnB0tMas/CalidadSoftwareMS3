package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum ResultadoAuditoria {

    EXITOSO("EXITOSO", "Exitoso"),
    FALLIDO("FALLIDO", "Fallido"),
    DENEGADO("DENEGADO", "Denegado");

    private final String code;
    private final String label;

    ResultadoAuditoria(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isExitoso() {
        return this == EXITOSO;
    }

    public boolean isFallido() {
        return this == FALLIDO;
    }

    public boolean isDenegado() {
        return this == DENEGADO;
    }

    public static ResultadoAuditoria fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El resultado de auditoría es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Resultado de auditoría no válido: " + code));
    }
}