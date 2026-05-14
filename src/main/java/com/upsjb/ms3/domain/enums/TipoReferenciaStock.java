package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum TipoReferenciaStock {

    VENTA_MS4("VENTA_MS4", "Venta registrada en MS4"),
    CARRITO_MS4("CARRITO_MS4", "Carrito o preventa registrada en MS4"),
    AJUSTE_MS3("AJUSTE_MS3", "Ajuste interno registrado en MS3");

    private final String code;
    private final String label;

    TipoReferenciaStock(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isMs4() {
        return this == VENTA_MS4 || this == CARRITO_MS4;
    }

    public static TipoReferenciaStock fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de referencia de stock es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de referencia de stock no válido: " + code));
    }
}