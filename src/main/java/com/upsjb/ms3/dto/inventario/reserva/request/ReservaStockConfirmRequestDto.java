// ruta: src/main/java/com/upsjb/ms3/dto/inventario/reserva/request/ReservaStockConfirmRequestDto.java
package com.upsjb.ms3.dto.inventario.reserva.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ReservaStockConfirmRequestDto(

        @NotBlank(message = "El motivo de confirmación es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}