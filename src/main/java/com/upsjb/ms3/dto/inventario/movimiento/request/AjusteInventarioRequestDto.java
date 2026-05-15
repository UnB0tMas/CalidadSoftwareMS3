// ruta: src/main/java/com/upsjb/ms3/dto/inventario/movimiento/request/AjusteInventarioRequestDto.java
package com.upsjb.ms3.dto.inventario.movimiento.request;

import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record AjusteInventarioRequestDto(

        @Valid
        @NotNull(message = "El SKU es obligatorio.")
        EntityReferenceDto sku,

        @Valid
        @NotNull(message = "El almacén es obligatorio.")
        EntityReferenceDto almacen,

        @NotNull(message = "El tipo de movimiento es obligatorio.")
        TipoMovimientoInventario tipoMovimiento,

        @NotNull(message = "El motivo del ajuste es obligatorio.")
        MotivoMovimientoInventario motivoMovimiento,

        @NotNull(message = "La cantidad de ajuste es obligatoria.")
        @Min(value = 1, message = "La cantidad de ajuste debe ser mayor o igual a 1.")
        Integer cantidad,

        @DecimalMin(value = "0.00", message = "El costo unitario no puede ser negativo.")
        BigDecimal costoUnitario,

        @Min(value = 0, message = "El stock físico esperado no puede ser negativo.")
        Integer stockFisicoEsperado,

        @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
        String referenciaIdExterno,

        @Size(max = 500, message = "La observación no debe superar 500 caracteres.")
        String observacion
) {
}