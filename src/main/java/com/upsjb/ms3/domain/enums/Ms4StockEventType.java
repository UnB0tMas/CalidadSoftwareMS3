package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum Ms4StockEventType {

    VENTA_STOCK_RESERVADO_PENDIENTE("VentaStockReservadoPendiente", "Venta con stock reservado pendiente"),
    VENTA_STOCK_CONFIRMADO_PENDIENTE("VentaStockConfirmadoPendiente", "Venta con stock confirmado pendiente"),
    VENTA_STOCK_LIBERADO_PENDIENTE("VentaStockLiberadoPendiente", "Venta con stock liberado pendiente"),
    VENTA_ANULADA_STOCK_PENDIENTE("VentaAnuladaStockPendiente", "Venta anulada con stock pendiente");

    private final String code;
    private final String label;

    Ms4StockEventType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isReserva() {
        return this == VENTA_STOCK_RESERVADO_PENDIENTE;
    }

    public boolean isConfirmacion() {
        return this == VENTA_STOCK_CONFIRMADO_PENDIENTE;
    }

    public boolean isLiberacion() {
        return this == VENTA_STOCK_LIBERADO_PENDIENTE;
    }

    public boolean isAnulacion() {
        return this == VENTA_ANULADA_STOCK_PENDIENTE;
    }

    public static Ms4StockEventType fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de evento de stock de MS4 es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de evento de stock de MS4 no válido: " + code));
    }
}