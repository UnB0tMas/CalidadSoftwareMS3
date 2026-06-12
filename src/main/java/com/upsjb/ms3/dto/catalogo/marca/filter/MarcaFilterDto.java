// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/marca/filter/MarcaFilterDto.java
package com.upsjb.ms3.dto.catalogo.marca.filter;

import com.upsjb.ms3.domain.value.SlugValue;

import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MarcaFilterDto(

        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,

        @Size(max = 50, message = "El código no debe superar 50 caracteres.")
        String codigo,

        @Size(max = 120, message = "El nombre no debe superar 120 caracteres.")
        String nombre,

        @Size(max = SlugValue.MAX_LENGTH, message = "El slug no debe superar 240 caracteres.")
        String slug,

        Boolean estado,

        @Valid
        DateRangeFilterDto fechaCreacion
) {
}
