// ruta: src/main/java/com/upsjb/ms3/dto/promocion/request/PromocionSkuDescuentoCreateRequestDto.java
package com.upsjb.ms3.dto.promocion.request;

import com.upsjb.ms3.domain.enums.TipoDescuento;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PromocionSkuDescuentoCreateRequestDto(

        @Valid
        @NotNull(message = "El SKU es obligatorio.")
        EntityReferenceDto sku,

        @NotNull(message = "El tipo de descuento es obligatorio.")
        TipoDescuento tipoDescuento,

        @NotNull(message = "El valor de descuento es obligatorio.")
        @DecimalMin(value = "0.00", inclusive = false, message = "El valor de descuento debe ser mayor que cero.")
        BigDecimal valorDescuento,

        BigDecimal precioFinalEstimado,

        BigDecimal margenEstimado,

        @Min(value = 1, message = "El límite de unidades debe ser mayor o igual a 1.")
        Integer limiteUnidades,

        @Min(value = 1, message = "La prioridad debe ser mayor o igual a 1.")
        Integer prioridad
) {
}