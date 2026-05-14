// ruta: src/main/java/com/upsjb/ms3/dto/precio/filter/PrecioSkuFilterDto.java
package com.upsjb.ms3.dto.precio.filter;

import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import lombok.Builder;

@Builder
public record PrecioSkuFilterDto(
        Long idSku,
        String codigoSku,
        Long idProducto,
        String codigoProducto,
        Moneda moneda,
        Boolean vigente,
        Boolean estado,
        @Valid DateRangeFilterDto fechaInicio,
        @Valid DateRangeFilterDto fechaCreacion
) {
}