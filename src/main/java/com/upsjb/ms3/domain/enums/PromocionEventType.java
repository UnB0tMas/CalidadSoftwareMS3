package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum PromocionEventType {

    PROMOCION_SNAPSHOT_ACTUALIZADA("PromocionSnapshotActualizada", "Snapshot de promoción actualizado"),
    PROMOCION_SNAPSHOT_CANCELADA("PromocionSnapshotCancelada", "Snapshot de promoción cancelado"),
    PROMOCION_SNAPSHOT_FINALIZADA("PromocionSnapshotFinalizada", "Snapshot de promoción finalizado");

    private final String code;
    private final String label;

    PromocionEventType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static PromocionEventType fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de evento de promoción es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de evento de promoción no válido: " + code));
    }
}