// ruta: src/main/java/com/upsjb/ms3/dto/inventario/compra/request/CompraInventarioDetalleRequestDto.java
package com.upsjb.ms3.dto.inventario.compra.request;

import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record CompraInventarioDetalleRequestDto(

        @Valid
        @NotNull(message = "El SKU es obligatorio.")
        EntityReferenceDto sku,

        @Valid
        @NotNull(message = "El almacén es obligatorio.")
        EntityReferenceDto almacen,

        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 1, message = "La cantidad debe ser mayor o igual a 1.")
        Integer cantidad,

        @NotNull(message = "El costo unitario es obligatorio.")
        @DecimalMin(value = "0.00", inclusive = false, message = "El costo unitario debe ser mayor que cero.")
        BigDecimal costoUnitario,

        @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo.")
        BigDecimal descuento,

        @DecimalMin(value = "0.00", message = "El impuesto no puede ser negativo.")
        BigDecimal impuesto
) {
}