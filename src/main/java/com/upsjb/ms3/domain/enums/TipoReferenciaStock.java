// ruta: src/main/java/com/upsjb/ms3/domain/enums/TipoReferenciaStock.java
package com.upsjb.ms3.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isMs4() {
        return this == VENTA_MS4 || this == CARRITO_MS4;
    }

    @JsonCreator
    public static TipoReferenciaStock fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de referencia de stock es obligatorio.");
        }

        String normalized = code.trim();

        return Arrays.stream(values())
                .filter(value ->
                        value.name().equalsIgnoreCase(normalized)
                                || value.code.equalsIgnoreCase(normalized)
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de referencia de stock no válido: " + code));
    }
}