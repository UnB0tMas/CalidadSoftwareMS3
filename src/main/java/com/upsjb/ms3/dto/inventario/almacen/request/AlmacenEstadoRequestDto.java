// ruta: src/main/java/com/upsjb/ms3/dto/inventario/almacen/request/AlmacenEstadoRequestDto.java
package com.upsjb.ms3.dto.inventario.almacen.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AlmacenEstadoRequestDto(

        @NotNull(message = "El estado es obligatorio.")
        Boolean estado,

        @NotBlank(message = "El motivo es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}