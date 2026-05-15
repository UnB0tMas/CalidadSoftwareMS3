// ruta: src/main/java/com/upsjb/ms3/dto/promocion/response/PromocionSkuDescuentoResponseDto.java
package com.upsjb.ms3.dto.promocion.response;

import com.upsjb.ms3.domain.enums.TipoDescuento;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PromocionSkuDescuentoResponseDto(
        Long idPromocionSkuDescuentoVersion,
        Long idPromocionVersion,
        Long idPromocion,
        String codigoPromocion,
        String nombrePromocion,
        Long idSku,
        String codigoSku,
        String codigoProducto,
        String nombreProducto,
        TipoDescuento tipoDescuento,
        BigDecimal valorDescuento,
        MoneyResponseDto precioBase,
        MoneyResponseDto precioFinalEstimado,
        MoneyResponseDto margenEstimado,
        Integer limiteUnidades,
        Integer prioridad,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}