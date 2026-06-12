// ruta: src/main/java/com/upsjb/ms3/dto/inventario/operacion/request/TransferenciaInventarioLineaRequestDto.java
package com.upsjb.ms3.dto.inventario.operacion.request;

import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record TransferenciaInventarioLineaRequestDto(

        @Valid
        @NotNull(message = "El SKU es obligatorio.")
        EntityReferenceDto sku,

        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 1, message = "La cantidad debe ser mayor o igual a 1.")
        Integer cantidad
) {
}
