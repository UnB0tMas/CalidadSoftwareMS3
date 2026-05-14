// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/request/ProductoVentaEstadoRequestDto.java
package com.upsjb.ms3.dto.catalogo.producto.request;

import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProductoVentaEstadoRequestDto(

        @NotNull(message = "El estado de venta es obligatorio.")
        EstadoProductoVenta estadoVenta,

        Boolean vendible,

        @NotBlank(message = "El motivo es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}