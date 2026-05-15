package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum TipoDescuento {

    PORCENTAJE("PORCENTAJE", "Porcentaje"),
    MONTO_FIJO("MONTO_FIJO", "Monto fijo"),
    PRECIO_FINAL("PRECIO_FINAL", "Precio final");

    private final String code;
    private final String label;

    TipoDescuento(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isPorcentaje() {
        return this == PORCENTAJE;
    }

    public boolean isMontoFijo() {
        return this == MONTO_FIJO;
    }

    public boolean isPrecioFinal() {
        return this == PRECIO_FINAL;
    }

    public static TipoDescuento fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de descuento es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de descuento no válido: " + code));
    }
}