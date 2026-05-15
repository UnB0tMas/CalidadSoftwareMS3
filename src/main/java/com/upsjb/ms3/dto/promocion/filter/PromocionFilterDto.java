// ruta: src/main/java/com/upsjb/ms3/dto/promocion/filter/PromocionFilterDto.java
package com.upsjb.ms3.dto.promocion.filter;

import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PromocionFilterDto(
        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,
        String codigo,
        String nombre,
        Boolean estado,
        @Valid DateRangeFilterDto fechaCreacion
) {
}