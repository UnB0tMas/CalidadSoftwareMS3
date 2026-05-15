package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum EstadoSku {

    ACTIVO("ACTIVO", "Activo", true, true),
    INACTIVO("INACTIVO", "Inactivo", false, false),
    DESCONTINUADO("DESCONTINUADO", "Descontinuado", false, false);

    private final String code;
    private final String label;
    private final boolean operativo;
    private final boolean editable;

    EstadoSku(String code, String label, boolean operativo, boolean editable) {
        this.code = code;
        this.label = label;
        this.operativo = operativo;
        this.editable = editable;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isOperativo() {
        return operativo;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isActivo() {
        return this == ACTIVO;
    }

    public static EstadoSku fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado del SKU es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de SKU no válido: " + code));
    }
}