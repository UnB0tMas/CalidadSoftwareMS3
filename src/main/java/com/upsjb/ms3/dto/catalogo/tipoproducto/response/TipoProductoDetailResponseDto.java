// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/tipoproducto/response/TipoProductoDetailResponseDto.java
package com.upsjb.ms3.dto.catalogo.tipoproducto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record TipoProductoDetailResponseDto(
        Long idTipoProducto,
        String codigo,
        String nombre,
        String descripcion,
        Boolean estado,
        Long cantidadAtributos,
        Long cantidadProductos,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}