// ruta: src/main/java/com/upsjb/ms3/dto/precio/filter/PrecioSkuFilterDto.java
package com.upsjb.ms3.dto.precio.filter;

import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PrecioSkuFilterDto(

        Long idPrecioHistorial,

        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,

        Long idSku,

        @Size(max = 100, message = "El código SKU no debe superar 100 caracteres.")
        String codigoSku,

        Long idProducto,

        @Size(max = 80, message = "El código de producto no debe superar 80 caracteres.")
        String codigoProducto,

        Moneda moneda,

        Boolean vigente,

        Long creadoPorIdUsuarioMs1,

        Boolean estado,

        @Valid
        DateRangeFilterDto fechaInicio,

        @Valid
        DateRangeFilterDto fechaFin,

        @Valid
        DateRangeFilterDto fechaCreacion
) {
}