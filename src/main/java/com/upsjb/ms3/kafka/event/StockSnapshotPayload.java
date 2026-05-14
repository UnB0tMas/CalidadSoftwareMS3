// ruta: src/main/java/com/upsjb/ms3/kafka/event/StockSnapshotPayload.java
package com.upsjb.ms3.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record StockSnapshotPayload(
        Long idStock,
        Long idSku,
        String codigoSku,
        String barcode,
        Long idProducto,
        String codigoProducto,
        String nombreProducto,
        Long idAlmacen,
        String codigoAlmacen,
        String nombreAlmacen,
        Integer stockFisico,
        Integer stockReservado,
        Integer stockDisponible,
        Integer stockMinimo,
        Integer stockMaximo,
        BigDecimal costoPromedioActual,
        BigDecimal ultimoCostoCompra,
        Boolean bajoStock,
        Boolean sobreStock,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}