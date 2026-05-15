package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum TipoProveedor {

    PERSONA_NATURAL("PERSONA_NATURAL", "Persona natural"),
    EMPRESA("EMPRESA", "Empresa");

    private final String code;
    private final String label;

    TipoProveedor(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isPersonaNatural() {
        return this == PERSONA_NATURAL;
    }

    public boolean isEmpresa() {
        return this == EMPRESA;
    }

    public static TipoProveedor fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de proveedor es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de proveedor no válido: " + code));
    }
}