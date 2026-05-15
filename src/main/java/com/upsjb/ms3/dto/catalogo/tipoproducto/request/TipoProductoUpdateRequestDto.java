// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/tipoproducto/request/TipoProductoUpdateRequestDto.java
package com.upsjb.ms3.dto.catalogo.tipoproducto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record TipoProductoUpdateRequestDto(

        @NotBlank(message = "El código del tipo de producto es obligatorio.")
        @Size(max = 50, message = "El código no debe superar 50 caracteres.")
        String codigo,

        @NotBlank(message = "El nombre del tipo de producto es obligatorio.")
        @Size(max = 120, message = "El nombre no debe superar 120 caracteres.")
        String nombre,

        @Size(max = 300, message = "La descripción no debe superar 300 caracteres.")
        String descripcion
) {
}