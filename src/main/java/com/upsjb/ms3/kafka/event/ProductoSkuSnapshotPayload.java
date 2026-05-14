// ruta: src/main/java/com/upsjb/ms3/kafka/event/ProductoSkuSnapshotPayload.java
package com.upsjb.ms3.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ProductoSkuSnapshotPayload(
        Long idSku,
        Long idProducto,
        String codigoProducto,
        String codigoSku,
        String barcode,
        String color,
        String talla,
        String material,
        String modelo,
        Integer stockMinimo,
        Integer stockMaximo,
        BigDecimal pesoGramos,
        BigDecimal altoCm,
        BigDecimal anchoCm,
        BigDecimal largoCm,
        String estadoSku,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}