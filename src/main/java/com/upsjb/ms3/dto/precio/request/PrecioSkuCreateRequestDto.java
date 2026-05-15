// ruta: src/main/java/com/upsjb/ms3/dto/precio/request/PrecioSkuCreateRequestDto.java
package com.upsjb.ms3.dto.precio.request;

import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PrecioSkuCreateRequestDto(

        @Valid
        @NotNull(message = "El SKU es obligatorio.")
        EntityReferenceDto sku,

        @NotNull(message = "El precio de venta es obligatorio.")
        @DecimalMin(value = "0.00", inclusive = false, message = "El precio de venta debe ser mayor que cero.")
        BigDecimal precioVenta,

        @NotNull(message = "La moneda es obligatoria.")
        Moneda moneda,

        LocalDateTime fechaInicio,

        @NotBlank(message = "El motivo del cambio de precio es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}