package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum EstadoReservaStock {

    RESERVADA("RESERVADA", "Reservada", true),
    CONFIRMADA("CONFIRMADA", "Confirmada", false),
    LIBERADA("LIBERADA", "Liberada", false),
    VENCIDA("VENCIDA", "Vencida", false),
    ANULADA("ANULADA", "Anulada", false);

    private final String code;
    private final String label;
    private final boolean pendiente;

    EstadoReservaStock(String code, String label, boolean pendiente) {
        this.code = code;
        this.label = label;
        this.pendiente = pendiente;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isPendiente() {
        return pendiente;
    }

    public boolean isReservada() {
        return this == RESERVADA;
    }

    public boolean isConfirmada() {
        return this == CONFIRMADA;
    }

    public boolean isLiberada() {
        return this == LIBERADA;
    }

    public boolean isFinalizada() {
        return this == CONFIRMADA || this == LIBERADA || this == VENCIDA || this == ANULADA;
    }

    public static EstadoReservaStock fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de reserva de stock es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de reserva de stock no válido: " + code));
    }
}