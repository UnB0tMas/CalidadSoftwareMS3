// ruta: src/main/java/com/upsjb/ms3/dto/reference/response/EmpleadoInventarioOptionDto.java
package com.upsjb.ms3.dto.reference.response;

import lombok.Builder;

@Builder
public record EmpleadoInventarioOptionDto(
        Long idEmpleadoSnapshot,
        Long idEmpleadoMs2,
        Long idUsuarioMs1,
        String codigoEmpleado,
        String nombresCompletos,
        String areaCodigo,
        String areaNombre,
        Boolean empleadoActivo,
        Boolean estado
) {
}