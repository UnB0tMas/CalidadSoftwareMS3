// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/atributo/request/TipoProductoAtributoAssignRequestDto.java
package com.upsjb.ms3.dto.catalogo.atributo.request;

import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record TipoProductoAtributoAssignRequestDto(

        @Valid
        @NotNull(message = "El tipo de producto es obligatorio.")
        EntityReferenceDto tipoProducto,

        @Valid
        @NotNull(message = "El atributo es obligatorio.")
        EntityReferenceDto atributo,

        Boolean requerido,

        @Min(value = 0, message = "El orden no puede ser negativo.")
        Integer orden,

        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}