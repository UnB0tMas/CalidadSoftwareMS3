package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum MotivoMovimientoInventario {

    COMPRA("COMPRA", "Compra de inventario"),
    VENTA("VENTA", "Venta registrada"),
    RESERVA_VENTA("RESERVA_VENTA", "Reserva para venta"),
    CONFIRMACION_VENTA("CONFIRMACION_VENTA", "Confirmación de venta"),
    LIBERACION_RESERVA("LIBERACION_RESERVA", "Liberación de reserva"),
    AJUSTE_POSITIVO("AJUSTE_POSITIVO", "Ajuste positivo"),
    AJUSTE_NEGATIVO("AJUSTE_NEGATIVO", "Ajuste negativo"),
    MERMA("MERMA", "Merma"),
    DEVOLUCION("DEVOLUCION", "Devolución"),
    TRASLADO("TRASLADO", "Traslado"),
    ANULACION_COMPENSATORIA("ANULACION_COMPENSATORIA", "Anulación compensatoria"),
    RECONCILIACION_MS4("RECONCILIACION_MS4", "Reconciliación desde MS4"),
    OTRO("OTRO", "Otro motivo");

    private final String code;
    private final String label;

    MotivoMovimientoInventario(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static MotivoMovimientoInventario fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El motivo del movimiento de inventario es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Motivo de movimiento de inventario no válido: " + code));
    }
}