// ruta: src/main/java/com/upsjb/ms3/dto/inventario/movimiento/request/MovimientoCompensatorioRequestDto.java
package com.upsjb.ms3.dto.inventario.movimiento.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MovimientoCompensatorioRequestDto(

        @NotNull(message = "El movimiento original es obligatorio.")
        Long idMovimientoOriginal,

        @NotBlank(message = "El motivo de compensación es obligatorio.")
        @Size(max = 500, message = "El motivo de compensación no debe superar 500 caracteres.")
        String motivoCompensacion
) {
}