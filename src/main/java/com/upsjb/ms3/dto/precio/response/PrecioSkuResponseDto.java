// ruta: src/main/java/com/upsjb/ms3/dto/precio/response/PrecioSkuResponseDto.java
package com.upsjb.ms3.dto.precio.response;

import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.dto.shared.IdCodigoNombreResponseDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PrecioSkuResponseDto(
        Long idPrecioHistorial,
        IdCodigoNombreResponseDto sku,
        Long idProducto,
        String codigoProducto,
        String nombreProducto,
        MoneyResponseDto precioVenta,
        Moneda moneda,
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