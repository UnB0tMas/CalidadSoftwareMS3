// ruta: src/main/java/com/upsjb/ms3/dto/inventario/movimiento/response/MovimientoInventarioResponseDto.java
package com.upsjb.ms3.dto.inventario.movimiento.response;

import com.upsjb.ms3.domain.enums.EstadoMovimientoInventario;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MovimientoInventarioResponseDto(
        Long idMovimiento,
        String codigoMovimiento,
        Boolean codigoGenerado,
        Long idSku,
        String codigoSku,
        String codigoProducto,
        String nombreProducto,
        Long idAlmacen,
        String codigoAlmacen,
        String nombreAlmacen,
        Long idCompraDetalle,
        Long idReservaStock,
        TipoMovimientoInventario tipoMovimiento,
        MotivoMovimientoInventario motivoMovimiento,
        Integer cantidad,
        MoneyResponseDto costoUnitario,
        MoneyResponseDto costoTotal,
        Integer stockAnterior,
        Integer stockNuevo,
        Integer variacionStock,
        String referenciaTipo,
        String referenciaIdExterno,
        String observacion,
        Long actorIdUsuarioMs1,
        Long actorIdEmpleadoMs2,
        RolSistema actorRol,
        String requestId,
        String correlationId,
        EstadoMovimientoInventario estadoMovimiento,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}