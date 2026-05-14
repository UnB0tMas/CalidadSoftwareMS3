// ruta: src/main/java/com/upsjb/ms3/dto/inventario/movimiento/response/KardexResponseDto.java
package com.upsjb.ms3.dto.inventario.movimiento.response;

import com.upsjb.ms3.domain.enums.EstadoMovimientoInventario;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record KardexResponseDto(
        Long idMovimiento,
        LocalDateTime fechaMovimiento,
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
        Integer entrada,
        Integer salida,
        Integer cantidad,
        Integer stockAnterior,
        Integer stockNuevo,
        Integer variacionStock,
        MoneyResponseDto costoUnitario,
        MoneyResponseDto costoTotal,
        String referenciaTipo,
        String referenciaIdExterno,
        String observacion,
        Long actorIdUsuarioMs1,
        Long actorIdEmpleadoMs2,
        RolSistema actorRol,
        EstadoMovimientoInventario estadoMovimiento,
        String requestId,
        String correlationId,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}