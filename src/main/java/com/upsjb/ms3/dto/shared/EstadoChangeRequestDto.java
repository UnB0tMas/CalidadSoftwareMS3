// ruta: src/main/java/com/upsjb/ms3/dto/shared/EstadoChangeRequestDto.java
package com.upsjb.ms3.dto.shared;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EstadoChangeRequestDto(

        @NotNull(message = "El estado es obligatorio.")
        Boolean estado,

        @NotBlank(message = "El motivo es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}