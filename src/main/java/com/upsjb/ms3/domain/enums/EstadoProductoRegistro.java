package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum EstadoProductoRegistro {

    BORRADOR("BORRADOR", "Borrador", false, true),
    ACTIVO("ACTIVO", "Activo", true, true),
    INACTIVO("INACTIVO", "Inactivo", false, false),
    DESCONTINUADO("DESCONTINUADO", "Descontinuado", false, false);

    private final String code;
    private final String label;
    private final boolean operativo;
    private final boolean editable;

    EstadoProductoRegistro(String code, String label, boolean operativo, boolean editable) {
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

    public boolean isBorrador() {
        return this == BORRADOR;
    }

    public boolean isActivo() {
        return this == ACTIVO;
    }

    public boolean isInactivo() {
        return this == INACTIVO;
    }

    public boolean isDescontinuado() {
        return this == DESCONTINUADO;
    }

    public static EstadoProductoRegistro fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de registro del producto es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de registro del producto no válido: " + code));
    }
}