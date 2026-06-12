// ruta: src/main/java/com/upsjb/ms3/dto/inventario/operacion/response/MovimientoInventarioLoteResponseDto.java
package com.upsjb.ms3.dto.inventario.operacion.response;

import com.upsjb.ms3.dto.inventario.movimiento.response.MovimientoInventarioResponseDto;
import java.util.List;
import lombok.Builder;

@Builder
public record MovimientoInventarioLoteResponseDto(
        String codigoOperacion,
        String tipoOperacion,
        Integer totalLineas,
        Integer totalMovimientos,
        List<MovimientoInventarioResponseDto> movimientos
) {
}
