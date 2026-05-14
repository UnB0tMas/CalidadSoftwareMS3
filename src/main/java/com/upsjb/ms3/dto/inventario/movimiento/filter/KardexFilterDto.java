// ruta: src/main/java/com/upsjb/ms3/dto/inventario/movimiento/filter/KardexFilterDto.java
package com.upsjb.ms3.dto.inventario.movimiento.filter;

import com.upsjb.ms3.domain.enums.EstadoMovimientoInventario;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record KardexFilterDto(

        Long idSku,

        String codigoSku,

        Long idProducto,

        String codigoProducto,

        Long idAlmacen,

        String codigoAlmacen,

        TipoMovimientoInventario tipoMovimiento,

        MotivoMovimientoInventario motivoMovimiento,

        EstadoMovimientoInventario estadoMovimiento,

        @Size(max = 50, message = "El tipo de referencia no debe superar 50 caracteres.")
        String referenciaTipo,

        @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
        String referenciaIdExterno,

        @Valid
        DateRangeFilterDto fechaMovimiento
) {
}