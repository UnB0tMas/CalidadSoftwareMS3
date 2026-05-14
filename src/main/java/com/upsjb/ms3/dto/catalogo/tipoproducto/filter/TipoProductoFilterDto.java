// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/tipoproducto/filter/TipoProductoFilterDto.java
package com.upsjb.ms3.dto.catalogo.tipoproducto.filter;

import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record TipoProductoFilterDto(

        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,

        @Size(max = 50, message = "El código no debe superar 50 caracteres.")
        String codigo,

        @Size(max = 120, message = "El nombre no debe superar 120 caracteres.")
        String nombre,

        Boolean estado,

        @Valid
        DateRangeFilterDto fechaCreacion
) {
}