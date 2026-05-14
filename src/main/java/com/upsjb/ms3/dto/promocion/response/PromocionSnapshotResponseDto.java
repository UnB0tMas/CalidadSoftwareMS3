// ruta: src/main/java/com/upsjb/ms3/dto/promocion/response/PromocionSnapshotResponseDto.java
package com.upsjb.ms3.dto.promocion.response;

import com.upsjb.ms3.domain.enums.EstadoPromocion;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record PromocionSnapshotResponseDto(
        Long idPromocionMs3,
        Long idPromocionVersionMs3,
        String codigoPromocion,
        String nombrePromocion,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        EstadoPromocion estadoPromocion,
        Boolean visiblePublico,
        Boolean vigente,
        List<PromocionSkuDescuentoResponseDto> descuentos,
        LocalDateTime updatedAt
) {
}