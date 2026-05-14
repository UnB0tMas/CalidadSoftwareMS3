// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/marca/response/MarcaDetailResponseDto.java
package com.upsjb.ms3.dto.catalogo.marca.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MarcaDetailResponseDto(
        Long idMarca,
        String codigo,
        String nombre,
        String slug,
        Boolean slugGenerado,
        String descripcion,
        Boolean estado,
        Long cantidadProductos,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}