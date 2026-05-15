// ruta: src/main/java/com/upsjb/ms3/dto/inventario/movimiento/filter/MovimientoInventarioFilterDto.java
package com.upsjb.ms3.dto.inventario.movimiento.filter;

import com.upsjb.ms3.domain.enums.EstadoMovimientoInventario;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MovimientoInventarioFilterDto(

        Long idMovimiento,

        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,

        @Size(max = 100, message = "El código de movimiento no debe superar 100 caracteres.")
        String codigoMovimiento,

        Long idSku,

        @Size(max = 100, message = "El código SKU no debe superar 100 caracteres.")
        String codigoSku,

        Long idProducto,

        @Size(max = 80, message = "El código de producto no debe superar 80 caracteres.")
        String codigoProducto,

        Long idAlmacen,

        @Size(max = 50, message = "El código de almacén no debe superar 50 caracteres.")
        String codigoAlmacen,

        Long idCompraDetalle,

        Long idReservaStock,

        TipoMovimientoInventario tipoMovimiento,

        MotivoMovimientoInventario motivoMovimiento,

        EstadoMovimientoInventario estadoMovimiento,

        @Size(max = 50, message = "El tipo de referencia no debe superar 50 caracteres.")
        String referenciaTipo,

        @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
        String referenciaIdExterno,

        Long actorIdUsuarioMs1,

        Long actorIdEmpleadoMs2,

        RolSistema actorRol,

        @Size(max = 100, message = "El requestId no debe superar 100 caracteres.")
        String requestId,

        @Size(max = 100, message = "El correlationId no debe superar 100 caracteres.")
        String correlationId,

        Boolean estado,

        @Valid
        DateRangeFilterDto fechaMovimiento
) {
}