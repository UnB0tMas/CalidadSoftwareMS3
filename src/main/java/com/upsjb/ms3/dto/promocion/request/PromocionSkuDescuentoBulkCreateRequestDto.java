package com.upsjb.ms3.dto.promocion.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Builder
public record PromocionSkuDescuentoBulkCreateRequestDto(

        @NotEmpty(message = "Debe seleccionar al menos una variante.")
        @Size(
                max = 500,
                message = "No se pueden registrar más de 500 variantes en una sola operación."
        )
        List<
                @Valid
                        PromocionSkuDescuentoCreateRequestDto
                > descuentos
) {
}