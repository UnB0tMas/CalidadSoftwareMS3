// ruta: src/main/java/com/upsjb/ms3/dto/empleado/filter/EmpleadoSnapshotMs2FilterDto.java
package com.upsjb.ms3.dto.empleado;

import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EmpleadoSnapshotMs2FilterDto(

        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,

        Long idEmpleadoSnapshot,

        Long idEmpleadoMs2,

        Long idUsuarioMs1,

        @Size(max = 50, message = "El código de empleado no debe superar 50 caracteres.")
        String codigoEmpleado,

        @Size(max = 250, message = "Los nombres completos no deben superar 250 caracteres.")
        String nombresCompletos,

        @Size(max = 50, message = "El código de área no debe superar 50 caracteres.")
        String areaCodigo,

        @Size(max = 120, message = "El nombre de área no debe superar 120 caracteres.")
        String areaNombre,

        Boolean empleadoActivo,

        Long snapshotVersion,

        Boolean estado,

        @Valid
        DateRangeFilterDto snapshotAt,

        @Valid
        DateRangeFilterDto fechaCreacion
) {
}