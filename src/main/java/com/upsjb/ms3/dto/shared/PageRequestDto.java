// ruta: src/main/java/com/upsjb/ms3/dto/shared/PageRequestDto.java
package com.upsjb.ms3.dto.shared;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PageRequestDto(

        @Min(value = 0, message = "La página no puede ser negativa.")
        Integer page,

        @Min(value = 1, message = "El tamaño mínimo de página es 1.")
        @Max(value = 100, message = "El tamaño máximo de página es 100.")
        Integer size,

        @Size(max = 80, message = "El campo de ordenamiento no debe superar 80 caracteres.")
        String sortBy,

        @Pattern(
                regexp = "^(?i)(ASC|DESC)$",
                message = "La dirección de ordenamiento debe ser ASC o DESC."
        )
        String sortDirection
) {

    public int safePage() {
        return page == null ? 0 : page;
    }

    public int safeSize() {
        return size == null ? 20 : size;
    }

    public String safeSortDirection() {
        return sortDirection == null || sortDirection.isBlank()
                ? "DESC"
                : sortDirection.trim().toUpperCase();
    }
}