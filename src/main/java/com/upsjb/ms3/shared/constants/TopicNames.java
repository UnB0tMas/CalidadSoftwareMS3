package com.upsjb.ms3.shared.constants;

public final class TopicNames {

    public static final String PRODUCTO_SNAPSHOT = "ms3.producto.snapshot.v1";
    public static final String STOCK_SNAPSHOT = "ms3.stock.snapshot.v1";
    public static final String PRECIO_SNAPSHOT = "ms3.precio.snapshot.v1";
    public static final String PROMOCION_SNAPSHOT = "ms3.promocion.snapshot.v1";
    public static final String MOVIMIENTO_INVENTARIO = "ms3.movimiento-inventario.v1";

    public static final String MS4_STOCK_COMMAND = "ms4.stock.command.v1";
    public static final String MS4_STOCK_RECONCILIATION = "ms4.stock.reconciliation.v1";

    public static final String DEAD_LETTER = "ms3.dead-letter.v1";

    private TopicNames() {
    }
}