// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/tipoproducto/response/TipoProductoResponseDto.java
package com.upsjb.ms3.dto.catalogo.tipoproducto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record TipoProductoResponseDto(
        Long idTipoProducto,
        String codigo,
        String nombre,
        String descripcion,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}