package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum PrecioEventType {

    PRECIO_SNAPSHOT_ACTUALIZADO("PrecioSnapshotActualizado", "Snapshot de precio actualizado");

    private final String code;
    private final String label;

    PrecioEventType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static PrecioEventType fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de evento de precio es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de evento de precio no válido: " + code));
    }
}