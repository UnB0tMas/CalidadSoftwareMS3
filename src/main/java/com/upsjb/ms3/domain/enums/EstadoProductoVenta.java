package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum EstadoProductoVenta {

    NO_VENDIBLE("NO_VENDIBLE", "No vendible", false, false),
    VENDIBLE("VENDIBLE", "Vendible", true, true),
    SOLO_VISIBLE("SOLO_VISIBLE", "Solo visible", true, false),
    AGOTADO("AGOTADO", "Agotado", true, false),
    PROXIMAMENTE("PROXIMAMENTE", "Próximamente", true, false);

    private final String code;
    private final String label;
    private final boolean visiblePublico;
    private final boolean seleccionableVenta;

    EstadoProductoVenta(String code, String label, boolean visiblePublico, boolean seleccionableVenta) {
        this.code = code;
        this.label = label;
        this.visiblePublico = visiblePublico;
        this.seleccionableVenta = seleccionableVenta;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isVisiblePublico() {
        return visiblePublico;
    }

    public boolean isSeleccionableVenta() {
        return seleccionableVenta;
    }

    public boolean isVendible() {
        return this == VENDIBLE;
    }

    public boolean isSoloVisible() {
        return this == SOLO_VISIBLE;
    }

    public boolean isAgotado() {
        return this == AGOTADO;
    }

    public boolean isProximamente() {
        return this == PROXIMAMENTE;
    }

    public static EstadoProductoVenta fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de venta del producto es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de venta del producto no válido: " + code));
    }
}