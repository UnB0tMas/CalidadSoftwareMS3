// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/categoria/filter/CategoriaFilterDto.java
package com.upsjb.ms3.dto.catalogo.categoria.filter;

import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CategoriaFilterDto(

        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,

        @Size(max = 50, message = "El código no debe superar 50 caracteres.")
        String codigo,

        @Size(max = 150, message = "El nombre no debe superar 150 caracteres.")
        String nombre,

        @Size(max = 180, message = "El slug no debe superar 180 caracteres.")
        String slug,

        Long idCategoriaPadre,

        @Min(value = 1, message = "El nivel mínimo es 1.")
        Integer nivel,

        Boolean estado,

        @Valid
        DateRangeFilterDto fechaCreacion
) {
}