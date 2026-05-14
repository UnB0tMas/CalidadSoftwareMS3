// ruta: src/main/java/com/upsjb/ms3/dto/promocion/response/PromocionResponseDto.java
package com.upsjb.ms3.dto.promocion.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PromocionResponseDto(
        Long idPromocion,
        String codigo,
        Boolean codigoGenerado,
        String nombre,
        String descripcion,
        Long creadoPorIdUsuarioMs1,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}