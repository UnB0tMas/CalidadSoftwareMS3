// ruta: src/main/java/com/upsjb/ms3/kafka/event/MovimientoInventarioPayload.java
package com.upsjb.ms3.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MovimientoInventarioPayload(
        Long idMovimiento,
        String codigoMovimiento,
        Long idSku,
        String codigoSku,
        Long idProducto,
        String codigoProducto,
        String nombreProducto,
        Long idAlmacen,
        String codigoAlmacen,
        String nombreAlmacen,
        Long idCompraDetalle,
        Long idReservaStock,
        String codigoReserva,
        String tipoMovimiento,
        String motivoMovimiento,
        Integer cantidad,
        BigDecimal costoUnitario,
        BigDecimal costoTotal,
        Integer stockAnterior,
        Integer stockNuevo,
        String referenciaTipo,
        String referenciaIdExterno,
        String observacion,
        Long actorIdUsuarioMs1,
        Long actorIdEmpleadoMs2,
        String actorRol,
        String requestId,
        String correlationId,
        String estadoMovimiento,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}