package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum StockEventType {

    STOCK_SNAPSHOT_ACTUALIZADO("StockSnapshotActualizado", "Snapshot de stock actualizado"),
    STOCK_RESERVADO("StockReservado", "Stock reservado"),
    STOCK_RESERVA_CONFIRMADA("StockReservaConfirmada", "Reserva de stock confirmada"),
    STOCK_RESERVA_LIBERADA("StockReservaLiberada", "Reserva de stock liberada"),
    STOCK_RESERVA_VENCIDA("StockReservaVencida", "Reserva de stock vencida"),
    STOCK_AJUSTADO("StockAjustado", "Stock ajustado"),
    MOVIMIENTO_INVENTARIO_REGISTRADO("MovimientoInventarioRegistrado", "Movimiento de inventario registrado");

    private final String code;
    private final String label;

    StockEventType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static StockEventType fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de evento de stock es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de evento de stock no válido: " + code));
    }
}