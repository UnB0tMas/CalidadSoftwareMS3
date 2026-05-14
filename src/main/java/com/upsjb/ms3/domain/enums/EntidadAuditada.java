package com.upsjb.ms3.domain.enums;

import java.util.Arrays;

public enum EntidadAuditada {

    CORRELATIVO_CODIGO("CORRELATIVO_CODIGO", "Correlativo de código"),
    TIPO_PRODUCTO("TIPO_PRODUCTO", "Tipo de producto"),
    CATEGORIA("CATEGORIA", "Categoría"),
    MARCA("MARCA", "Marca"),
    ATRIBUTO("ATRIBUTO", "Atributo"),
    TIPO_PRODUCTO_ATRIBUTO("TIPO_PRODUCTO_ATRIBUTO", "Tipo producto atributo"),
    PRODUCTO("PRODUCTO", "Producto"),
    PRODUCTO_SKU("PRODUCTO_SKU", "SKU de producto"),
    PRODUCTO_ATRIBUTO_VALOR("PRODUCTO_ATRIBUTO_VALOR", "Valor de atributo de producto"),
    SKU_ATRIBUTO_VALOR("SKU_ATRIBUTO_VALOR", "Valor de atributo de SKU"),
    PRODUCTO_IMAGEN_CLOUDINARY("PRODUCTO_IMAGEN_CLOUDINARY", "Imagen Cloudinary de producto"),
    PROVEEDOR("PROVEEDOR", "Proveedor"),
    ALMACEN("ALMACEN", "Almacén"),
    STOCK_SKU("STOCK_SKU", "Stock por SKU"),
    EMPLEADO_SNAPSHOT_MS2("EMPLEADO_SNAPSHOT_MS2", "Snapshot de empleado MS2"),
    EMPLEADO_INVENTARIO_PERMISO("EMPLEADO_INVENTARIO_PERMISO", "Permiso de inventario de empleado"),
    PRECIO_SKU_HISTORIAL("PRECIO_SKU_HISTORIAL", "Historial de precio de SKU"),
    PROMOCION("PROMOCION", "Promoción"),
    PROMOCION_VERSION("PROMOCION_VERSION", "Versión de promoción"),
    PROMOCION_SKU_DESCUENTO_VERSION("PROMOCION_SKU_DESCUENTO_VERSION", "Descuento de SKU en promoción"),
    COMPRA_INVENTARIO("COMPRA_INVENTARIO", "Compra de inventario"),
    COMPRA_INVENTARIO_DETALLE("COMPRA_INVENTARIO_DETALLE", "Detalle de compra de inventario"),
    RESERVA_STOCK("RESERVA_STOCK", "Reserva de stock"),
    MOVIMIENTO_INVENTARIO("MOVIMIENTO_INVENTARIO", "Movimiento de inventario"),
    AUDITORIA_FUNCIONAL("AUDITORIA_FUNCIONAL", "Auditoría funcional"),
    EVENTO_DOMINIO_OUTBOX("EVENTO_DOMINIO_OUTBOX", "Evento dominio outbox"),
    CLOUDINARY("CLOUDINARY", "Cloudinary"),
    KAFKA("KAFKA", "Kafka"),
    MS2("MS2", "Microservicio MS2"),
    MS4("MS4", "Microservicio MS4"),
    SISTEMA("SISTEMA", "Sistema");

    private final String code;
    private final String label;

    EntidadAuditada(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static EntidadAuditada fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("La entidad auditada es obligatoria.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Entidad auditada no válida: " + code));
    }
}