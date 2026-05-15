// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/marca/response/MarcaResponseDto.java
package com.upsjb.ms3.dto.catalogo.marca.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MarcaResponseDto(
        Long idMarca,
        String codigo,
        String nombre,
        String slug,
        Boolean slugGenerado,
        String descripcion,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}