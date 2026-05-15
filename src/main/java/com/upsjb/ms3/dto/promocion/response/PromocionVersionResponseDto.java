// ruta: src/main/java/com/upsjb/ms3/dto/promocion/response/PromocionVersionResponseDto.java
package com.upsjb.ms3.dto.promocion.response;

import com.upsjb.ms3.domain.enums.EstadoPromocion;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record PromocionVersionResponseDto(
        Long idPromocionVersion,
        Long idPromocion,
        String codigoPromocion,
        String nombrePromocion,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        EstadoPromocion estadoPromocion,
        Boolean visiblePublico,
        Boolean vigente,
        String motivo,
        Long creadoPorIdUsuarioMs1,
        Boolean estado,
        List<PromocionSkuDescuentoResponseDto> descuentos,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}