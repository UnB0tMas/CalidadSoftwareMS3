// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/request/ProductoEstadoRegistroRequestDto.java
package com.upsjb.ms3.dto.catalogo.producto.request;

import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProductoEstadoRegistroRequestDto(

        @NotNull(message = "El estado de registro es obligatorio.")
        EstadoProductoRegistro estadoRegistro,

        @NotBlank(message = "El motivo es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}