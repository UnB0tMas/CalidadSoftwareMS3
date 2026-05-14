// ruta: src/main/java/com/upsjb/ms3/dto/inventario/reserva/request/ReservaStockCreateRequestDto.java
package com.upsjb.ms3.dto.inventario.reserva.request;

import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ReservaStockCreateRequestDto(

        @Valid
        @NotNull(message = "El SKU es obligatorio.")
        EntityReferenceDto sku,

        @Valid
        @NotNull(message = "El almacén es obligatorio.")
        EntityReferenceDto almacen,

        @NotNull(message = "El tipo de referencia es obligatorio.")
        TipoReferenciaStock referenciaTipo,

        @NotBlank(message = "La referencia externa es obligatoria.")
        @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
        String referenciaIdExterno,

        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 1, message = "La cantidad debe ser mayor o igual a 1.")
        Integer cantidad,

        @Future(message = "La fecha de expiración debe ser futura.")
        LocalDateTime expiresAt,

        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}