// ruta: src/main/java/com/upsjb/ms3/dto/promocion/response/PromocionSkuDescuentoCalculoResponseDto.java
package com.upsjb.ms3.dto.promocion.response;

import com.upsjb.ms3.domain.enums.TipoDescuento;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PromocionSkuDescuentoCalculoResponseDto(
        Long idPromocionVersion,
        Long idSku,
        String codigoSku,
        String codigoProducto,
        String nombreProducto,
        TipoDescuento tipoDescuento,
        BigDecimal valorDescuento,
        MoneyResponseDto precioBase,
        MoneyResponseDto precioFinalEstimado,
        MoneyResponseDto margenEstimado,
        Boolean generaMargenNegativo,
        Integer limiteUnidades,
        Integer prioridad
) {
}