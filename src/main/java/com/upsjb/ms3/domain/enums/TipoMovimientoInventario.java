package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum TipoMovimientoInventario {

    ENTRADA_COMPRA("ENTRADA_COMPRA", "Entrada por compra", true, false, true),
    ENTRADA_AJUSTE("ENTRADA_AJUSTE", "Entrada por ajuste", true, false, false),
    ENTRADA_DEVOLUCION("ENTRADA_DEVOLUCION", "Entrada por devolución", true, false, false),
    SALIDA_VENTA("SALIDA_VENTA", "Salida por venta", false, true, true),
    SALIDA_AJUSTE("SALIDA_AJUSTE", "Salida por ajuste", false, true, false),
    SALIDA_MERMA("SALIDA_MERMA", "Salida por merma", false, true, false),
    SALIDA_TRASLADO("SALIDA_TRASLADO", "Salida por traslado", false, true, false),
    ENTRADA_TRASLADO("ENTRADA_TRASLADO", "Entrada por traslado", true, false, false),
    RESERVA_VENTA("RESERVA_VENTA", "Reserva de stock para venta", false, false, true),
    LIBERACION_RESERVA("LIBERACION_RESERVA", "Liberación de reserva", false, false, true),
    CONFIRMACION_VENTA("CONFIRMACION_VENTA", "Confirmación de venta", false, true, true),
    ANULACION_COMPENSATORIA("ANULACION_COMPENSATORIA", "Anulación compensatoria", false, false, false);

    private final String code;
    private final String label;
    private final boolean entradaFisica;
    private final boolean salidaFisica;
    private final boolean relacionadoVentaOCompra;

    TipoMovimientoInventario(
            String code,
            String label,
            boolean entradaFisica,
            boolean salidaFisica,
            boolean relacionadoVentaOCompra
    ) {
        this.code = code;
        this.label = label;
        this.entradaFisica = entradaFisica;
        this.salidaFisica = salidaFisica;
        this.relacionadoVentaOCompra = relacionadoVentaOCompra;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isEntradaFisica() {
        return entradaFisica;
    }

    public boolean isSalidaFisica() {
        return salidaFisica;
    }

    public boolean isRelacionadoVentaOCompra() {
        return relacionadoVentaOCompra;
    }

    public boolean isReserva() {
        return this == RESERVA_VENTA;
    }

    public boolean isLiberacionReserva() {
        return this == LIBERACION_RESERVA;
    }

    public boolean isConfirmacionVenta() {
        return this == CONFIRMACION_VENTA;
    }

    public static TipoMovimientoInventario fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de movimiento de inventario es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de movimiento de inventario no válido: " + code));
    }
}