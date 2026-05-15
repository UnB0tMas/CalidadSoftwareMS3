package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum GeneroObjetivo {

    HOMBRE("HOMBRE", "Hombre"),
    MUJER("MUJER", "Mujer"),
    UNISEX("UNISEX", "Unisex"),
    NIÑO("NIÑO", "Niño"),
    NIÑA("NIÑA", "Niña"),
    GENERAL("GENERAL", "General");

    private final String code;
    private final String label;

    GeneroObjetivo(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static GeneroObjetivo fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El género objetivo es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Género objetivo no válido: " + code));
    }
}