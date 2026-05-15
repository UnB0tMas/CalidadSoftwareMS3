// ruta: src/main/java/com/upsjb/ms3/dto/empleado/response/EmpleadoSnapshotMs2ResponseDto.java
package com.upsjb.ms3.dto.empleado.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record EmpleadoSnapshotMs2ResponseDto(
        Long idEmpleadoSnapshot,
        Long idEmpleadoMs2,
        Long idUsuarioMs1,
        String codigoEmpleado,
        String nombresCompletos,
        String areaCodigo,
        String areaNombre,
        Boolean empleadoActivo,
        Long snapshotVersion,
        LocalDateTime snapshotAt,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}