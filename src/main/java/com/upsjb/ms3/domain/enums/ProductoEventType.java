package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum ProductoEventType {

    PRODUCTO_SNAPSHOT_CREADO("ProductoSnapshotCreado", "Snapshot de producto creado"),
    PRODUCTO_SNAPSHOT_ACTUALIZADO("ProductoSnapshotActualizado", "Snapshot de producto actualizado"),
    PRODUCTO_SNAPSHOT_PUBLICADO("ProductoSnapshotPublicado", "Snapshot de producto publicado"),
    PRODUCTO_SNAPSHOT_DESPUBLICADO("ProductoSnapshotDespublicado", "Snapshot de producto despublicado"),
    PRODUCTO_SNAPSHOT_INACTIVADO("ProductoSnapshotInactivado", "Snapshot de producto inactivado"),
    SKU_SNAPSHOT_CREADO("SkuSnapshotCreado", "Snapshot de SKU creado"),
    SKU_SNAPSHOT_ACTUALIZADO("SkuSnapshotActualizado", "Snapshot de SKU actualizado"),
    SKU_SNAPSHOT_INACTIVADO("SkuSnapshotInactivado", "Snapshot de SKU inactivado"),
    IMAGEN_PRODUCTO_SNAPSHOT_ACTUALIZADO("ImagenProductoSnapshotActualizado", "Snapshot de imagen de producto actualizado");

    private final String code;
    private final String label;

    ProductoEventType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ProductoEventType fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de evento de producto es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de evento de producto no válido: " + code));
    }
}