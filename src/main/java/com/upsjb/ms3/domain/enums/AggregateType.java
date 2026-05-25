// ruta: src/main/java/com/upsjb/ms3/domain/enums/AggregateType.java
package com.upsjb.ms3.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum AggregateType {

    PRODUCTO("PRODUCTO", "Producto"),
    SKU("SKU", "SKU"),
    STOCK("STOCK", "Stock"),
    PRECIO("PRECIO", "Precio"),
    PROMOCION("PROMOCION", "Promoción"),
    MOVIMIENTO_INVENTARIO("MOVIMIENTO_INVENTARIO", "Movimiento de inventario"),
    COMPRA_INVENTARIO("COMPRA_INVENTARIO", "Compra de inventario"),
    RESERVA_STOCK("RESERVA_STOCK", "Reserva de stock"),
    IMAGEN_PRODUCTO("IMAGEN_PRODUCTO", "Imagen de producto"),
    EMPLEADO_INVENTARIO_PERMISO("EMPLEADO_INVENTARIO_PERMISO", "Permiso de inventario"),
    OUTBOX("OUTBOX", "Outbox");

    private final String code;
    private final String label;

    AggregateType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static AggregateType fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El aggregate type es obligatorio.");
        }

        String normalized = code.trim();

        return Arrays.stream(values())
                .filter(value ->
                        value.name().equalsIgnoreCase(normalized)
                                || value.code.equalsIgnoreCase(normalized)
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Aggregate type no válido: " + code));
    }
}