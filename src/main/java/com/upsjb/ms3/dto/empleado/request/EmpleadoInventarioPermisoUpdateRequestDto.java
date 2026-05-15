// ruta: src/main/java/com/upsjb/ms3/dto/empleado/request/EmpleadoInventarioPermisoUpdateRequestDto.java
package com.upsjb.ms3.dto.empleado.request;

import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record EmpleadoInventarioPermisoUpdateRequestDto(

        @Valid
        @NotNull(message = "El empleado es obligatorio.")
        EntityReferenceDto empleado,

        Boolean puedeCrearProductoBasico,

        Boolean puedeEditarProductoBasico,

        Boolean puedeRegistrarEntrada,

        Boolean puedeRegistrarSalida,

        Boolean puedeRegistrarAjuste,

        Boolean puedeConsultarKardex,

        Boolean puedeGestionarImagenes,

        Boolean puedeActualizarAtributos,

        LocalDateTime fechaInicio,

        LocalDateTime fechaFin,

        @NotBlank(message = "El motivo es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}