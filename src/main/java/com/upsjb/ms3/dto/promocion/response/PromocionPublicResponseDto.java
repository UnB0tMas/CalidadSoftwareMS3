// ruta: src/main/java/com/upsjb/ms3/dto/promocion/response/PromocionPublicResponseDto.java
package com.upsjb.ms3.dto.promocion.response;

import com.upsjb.ms3.domain.enums.EstadoPromocion;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record PromocionPublicResponseDto(
        Long idPromocion,
        Long idPromocionVersion,
        String codigo,
        String nombre,
        String descripcion,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        EstadoPromocion estadoPromocion,
        List<PromocionSkuDescuentoResponseDto> descuentos
) {
}