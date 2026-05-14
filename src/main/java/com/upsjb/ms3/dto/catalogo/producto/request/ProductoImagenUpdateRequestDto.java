// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/request/ProductoImagenUpdateRequestDto.java
package com.upsjb.ms3.dto.catalogo.producto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProductoImagenUpdateRequestDto(

        @Size(max = 250, message = "El texto alternativo no debe superar 250 caracteres.")
        String altText,

        @Size(max = 180, message = "El título no debe superar 180 caracteres.")
        String titulo,

        @Min(value = 0, message = "El orden no puede ser negativo.")
        Integer orden
) {
}