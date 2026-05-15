// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/request/ProductoPublicacionRequestDto.java
package com.upsjb.ms3.dto.catalogo.producto.request;

import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ProductoPublicacionRequestDto(

        @NotNull(message = "El estado de publicación es obligatorio.")
        EstadoProductoPublicacion estadoPublicacion,

        Boolean visiblePublico,

        LocalDateTime fechaPublicacionInicio,

        LocalDateTime fechaPublicacionFin,

        @NotBlank(message = "El motivo es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}