package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum TipoEventoAuditoria {

    PRODUCTO_CREADO("PRODUCTO_CREADO", "Producto creado"),
    PRODUCTO_ACTUALIZADO("PRODUCTO_ACTUALIZADO", "Producto actualizado"),
    PRODUCTO_INACTIVADO("PRODUCTO_INACTIVADO", "Producto inactivado"),
    PRODUCTO_DESCONTINUADO("PRODUCTO_DESCONTINUADO", "Producto descontinuado"),
    PRODUCTO_PUBLICADO("PRODUCTO_PUBLICADO", "Producto publicado"),
    PRODUCTO_DESPUBLICADO("PRODUCTO_DESPUBLICADO", "Producto despublicado"),
    SKU_CREADO("SKU_CREADO", "SKU creado"),
    SKU_ACTUALIZADO("SKU_ACTUALIZADO", "SKU actualizado"),
    SKU_INACTIVADO("SKU_INACTIVADO", "SKU inactivado"),
    PRECIO_ACTUALIZADO("PRECIO_ACTUALIZADO", "Precio actualizado"),
    PROMOCION_CREADA("PROMOCION_CREADA", "Promoción creada"),
    PROMOCION_ACTUALIZADA("PROMOCION_ACTUALIZADA", "Promoción actualizada"),
    PROMOCION_VERSIONADA("PROMOCION_VERSIONADA", "Promoción versionada"),
    PROMOCION_CANCELADA("PROMOCION_CANCELADA", "Promoción cancelada"),
    IMAGEN_PRODUCTO_SUBIDA("IMAGEN_PRODUCTO_SUBIDA", "Imagen de producto subida"),
    IMAGEN_PRODUCTO_INACTIVADA("IMAGEN_PRODUCTO_INACTIVADA", "Imagen de producto inactivada"),
    PROVEEDOR_CREADO("PROVEEDOR_CREADO", "Proveedor creado"),
    PROVEEDOR_ACTUALIZADO("PROVEEDOR_ACTUALIZADO", "Proveedor actualizado"),
    PROVEEDOR_INACTIVADO("PROVEEDOR_INACTIVADO", "Proveedor inactivado"),
    COMPRA_REGISTRADA("COMPRA_REGISTRADA", "Compra registrada"),
    COMPRA_CONFIRMADA("COMPRA_CONFIRMADA", "Compra confirmada"),
    COMPRA_ANULADA("COMPRA_ANULADA", "Compra anulada"),
    ENTRADA_INVENTARIO_REGISTRADA("ENTRADA_INVENTARIO_REGISTRADA", "Entrada de inventario registrada"),
    SALIDA_INVENTARIO_REGISTRADA("SALIDA_INVENTARIO_REGISTRADA", "Salida de inventario registrada"),
    AJUSTE_STOCK_REGISTRADO("AJUSTE_STOCK_REGISTRADO", "Ajuste de stock registrado"),
    RESERVA_STOCK_CREADA("RESERVA_STOCK_CREADA", "Reserva de stock creada"),
    RESERVA_STOCK_CONFIRMADA("RESERVA_STOCK_CONFIRMADA", "Reserva de stock confirmada"),
    RESERVA_STOCK_LIBERADA("RESERVA_STOCK_LIBERADA", "Reserva de stock liberada"),
    MOVIMIENTO_KARDEX_REGISTRADO("MOVIMIENTO_KARDEX_REGISTRADO", "Movimiento de kardex registrado"),
    PERMISO_INVENTARIO_OTORGADO("PERMISO_INVENTARIO_OTORGADO", "Permiso de inventario otorgado"),
    PERMISO_INVENTARIO_REVOCADO("PERMISO_INVENTARIO_REVOCADO", "Permiso de inventario revocado"),
    EVENTO_KAFKA_REGISTRADO("EVENTO_KAFKA_REGISTRADO", "Evento Kafka registrado"),
    EVENTO_KAFKA_PUBLICADO("EVENTO_KAFKA_PUBLICADO", "Evento Kafka publicado"),
    EVENTO_KAFKA_FALLIDO("EVENTO_KAFKA_FALLIDO", "Evento Kafka fallido"),
    EVENTO_KAFKA_REINTENTADO("EVENTO_KAFKA_REINTENTADO", "Evento Kafka reintentado"),
    ACCESO_DENEGADO("ACCESO_DENEGADO", "Acceso denegado"),
    VALIDACION_FALLIDA("VALIDACION_FALLIDA", "Validación fallida"),
    ERROR_SISTEMA("ERROR_SISTEMA", "Error del sistema");

    private final String code;
    private final String label;

    TipoEventoAuditoria(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static TipoEventoAuditoria fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de evento de auditoría es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de evento de auditoría no válido: " + code));
    }
}