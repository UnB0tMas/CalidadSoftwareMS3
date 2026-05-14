// ruta: src/main/java/com/upsjb/ms3/dto/empleado/response/EmpleadoInventarioPermisoResponseDto.java
package com.upsjb.ms3.dto.empleado.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record EmpleadoInventarioPermisoResponseDto(
        Long idPermisoHistorial,
        EmpleadoSnapshotMs2ResponseDto empleado,
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
        Boolean vigente,
        Long otorgadoPorIdUsuarioMs1,
        Long revocadoPorIdUsuarioMs1,
        String motivo,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}