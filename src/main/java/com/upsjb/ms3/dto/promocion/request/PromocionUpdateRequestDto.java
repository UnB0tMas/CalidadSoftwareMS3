// ruta: src/main/java/com/upsjb/ms3/dto/promocion/request/PromocionUpdateRequestDto.java
package com.upsjb.ms3.dto.promocion.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PromocionUpdateRequestDto(

        @NotBlank(message = "El nombre de la promoción es obligatorio.")
        @Size(max = 180, message = "El nombre no debe superar 180 caracteres.")
        String nombre,

        @Size(max = 500, message = "La descripción no debe superar 500 caracteres.")
        String descripcion
) {
}