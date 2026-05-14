// ruta: src/main/java/com/upsjb/ms3/dto/promocion/filter/PromocionSkuDescuentoFilterDto.java
package com.upsjb.ms3.dto.promocion.filter;

import com.upsjb.ms3.domain.enums.TipoDescuento;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PromocionSkuDescuentoFilterDto(
        String search,
        Long idPromocionSkuDescuentoVersion,
        Long idPromocionVersion,
        Long idPromocion,
        String codigoPromocion,
        String nombrePromocion,
        Long idSku,
        String codigoSku,
        String barcode,
        Long idProducto,
        String codigoProducto,
        String nombreProducto,
        TipoDescuento tipoDescuento,
        BigDecimal valorDescuentoMin,
        BigDecimal valorDescuentoMax,
        Boolean estado,
        @Valid DateRangeFilterDto fechaCreacion
) {
}