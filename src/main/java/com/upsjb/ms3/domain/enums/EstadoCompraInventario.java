package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum EstadoCompraInventario {

    BORRADOR("BORRADOR", "Borrador", true, false),
    CONFIRMADA("CONFIRMADA", "Confirmada", false, true),
    ANULADA("ANULADA", "Anulada", false, false);

    private final String code;
    private final String label;
    private final boolean editable;
    private final boolean impactaStock;

    EstadoCompraInventario(String code, String label, boolean editable, boolean impactaStock) {
        this.code = code;
        this.label = label;
        this.editable = editable;
        this.impactaStock = impactaStock;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isImpactaStock() {
        return impactaStock;
    }

    public boolean isBorrador() {
        return this == BORRADOR;
    }

    public boolean isConfirmada() {
        return this == CONFIRMADA;
    }

    public boolean isAnulada() {
        return this == ANULADA;
    }

    public static EstadoCompraInventario fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de compra de inventario es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de compra de inventario no válido: " + code));
    }
}