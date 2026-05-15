// ruta: src/main/java/com/upsjb/ms3/kafka/event/PrecioSnapshotPayload.java
package com.upsjb.ms3.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PrecioSnapshotPayload(
        Long idPrecioHistorial,
        Long idSku,
        String codigoSku,
        Long idProducto,
        String codigoProducto,
        String nombreProducto,
        BigDecimal precioVenta,
        String moneda,
        String simboloMoneda,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Boolean vigente,
        String motivo,
        Long creadoPorIdUsuarioMs1,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}