// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/atributo/request/TipoProductoAtributoUpdateRequestDto.java
package com.upsjb.ms3.dto.catalogo.atributo.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record TipoProductoAtributoUpdateRequestDto(

        Boolean requerido,

        @Min(value = 0, message = "El orden no puede ser negativo.")
        Integer orden,

        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}