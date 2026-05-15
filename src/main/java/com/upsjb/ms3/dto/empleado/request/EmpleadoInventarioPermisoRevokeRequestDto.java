// ruta: src/main/java/com/upsjb/ms3/dto/empleado/request/EmpleadoInventarioPermisoRevokeRequestDto.java
package com.upsjb.ms3.dto.empleado.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record EmpleadoInventarioPermisoRevokeRequestDto(

        LocalDateTime fechaFin,

        @NotBlank(message = "El motivo de revocación es obligatorio.")
        @Size(max = 500, message = "El motivo de revocación no debe superar 500 caracteres.")
        String motivo
) {
}