// ruta: src/main/java/com/upsjb/ms3/dto/promocion/response/PromocionDetailResponseDto.java
package com.upsjb.ms3.dto.promocion.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record PromocionDetailResponseDto(
        Long idPromocion,
        String codigo,
        Boolean codigoGenerado,
        String nombre,
        String descripcion,
        Boolean estado,
        List<PromocionVersionResponseDto> versiones,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}