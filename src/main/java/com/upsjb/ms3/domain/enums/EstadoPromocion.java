package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum EstadoPromocion {

    BORRADOR("BORRADOR", "Borrador", false, true),
    PROGRAMADA("PROGRAMADA", "Programada", true, true),
    ACTIVA("ACTIVA", "Activa", true, false),
    FINALIZADA("FINALIZADA", "Finalizada", false, false),
    CANCELADA("CANCELADA", "Cancelada", false, false);

    private final String code;
    private final String label;
    private final boolean visibleEnFlujoPublico;
    private final boolean editable;

    EstadoPromocion(String code, String label, boolean visibleEnFlujoPublico, boolean editable) {
        this.code = code;
        this.label = label;
        this.visibleEnFlujoPublico = visibleEnFlujoPublico;
        this.editable = editable;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isVisibleEnFlujoPublico() {
        return visibleEnFlujoPublico;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isActiva() {
        return this == ACTIVA;
    }

    public boolean isFinalizada() {
        return this == FINALIZADA || this == CANCELADA;
    }

    public static EstadoPromocion fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de promoción es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de promoción no válido: " + code));
    }
}