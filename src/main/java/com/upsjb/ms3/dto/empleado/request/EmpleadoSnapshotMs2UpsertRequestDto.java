// ruta: src/main/java/com/upsjb/ms3/dto/empleado/request/EmpleadoSnapshotMs2UpsertRequestDto.java
package com.upsjb.ms3.dto.empleado.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record EmpleadoSnapshotMs2UpsertRequestDto(

        @NotNull(message = "El id del empleado en MS2 es obligatorio.")
        Long idEmpleadoMs2,

        @NotNull(message = "El id del usuario en MS1 es obligatorio.")
        Long idUsuarioMs1,

        @NotBlank(message = "El código del empleado es obligatorio.")
        @Size(max = 50, message = "El código del empleado no debe superar 50 caracteres.")
        String codigoEmpleado,

        @NotBlank(message = "Los nombres completos son obligatorios.")
        @Size(max = 250, message = "Los nombres completos no deben superar 250 caracteres.")
        String nombresCompletos,

        @Size(max = 50, message = "El código del área no debe superar 50 caracteres.")
        String areaCodigo,

        @Size(max = 120, message = "El nombre del área no debe superar 120 caracteres.")
        String areaNombre,

        @NotNull(message = "El estado activo del empleado es obligatorio.")
        Boolean empleadoActivo,

        Long snapshotVersion,

        LocalDateTime snapshotAt,

        Boolean estado
) {
}