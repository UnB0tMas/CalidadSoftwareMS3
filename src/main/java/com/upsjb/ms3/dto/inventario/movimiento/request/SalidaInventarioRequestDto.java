// ruta: src/main/java/com/upsjb/ms3/dto/inventario/movimiento/request/SalidaInventarioRequestDto.java
package com.upsjb.ms3.dto.inventario.movimiento.request;

import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record SalidaInventarioRequestDto(

        @Valid
        @NotNull(message = "El SKU es obligatorio.")
        EntityReferenceDto sku,

        @Valid
        @NotNull(message = "El almacén es obligatorio.")
        EntityReferenceDto almacen,

        @NotNull(message = "El tipo de movimiento es obligatorio.")
        TipoMovimientoInventario tipoMovimiento,

        @NotNull(message = "El motivo del movimiento es obligatorio.")
        MotivoMovimientoInventario motivoMovimiento,

        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 1, message = "La cantidad debe ser mayor o igual a 1.")
        Integer cantidad,

        @Size(max = 50, message = "El tipo de referencia no debe superar 50 caracteres.")
        String referenciaTipo,

        @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
        String referenciaIdExterno,

        @Size(max = 500, message = "La observación no debe superar 500 caracteres.")
        String observacion
) {
}