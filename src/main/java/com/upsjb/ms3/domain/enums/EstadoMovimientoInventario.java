package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum EstadoMovimientoInventario {

    REGISTRADO("REGISTRADO", "Registrado", true),
    COMPENSADO("COMPENSADO", "Compensado", false),
    ANULADO_LOGICO("ANULADO_LOGICO", "Anulado lógico", false);

    private final String code;
    private final String label;
    private final boolean vigente;

    EstadoMovimientoInventario(String code, String label, boolean vigente) {
        this.code = code;
        this.label = label;
        this.vigente = vigente;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isVigente() {
        return vigente;
    }

    public boolean isRegistrado() {
        return this == REGISTRADO;
    }

    public static EstadoMovimientoInventario fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado del movimiento de inventario es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de movimiento de inventario no válido: " + code));
    }
}