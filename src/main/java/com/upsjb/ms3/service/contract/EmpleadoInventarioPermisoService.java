// ruta: src/main/java/com/upsjb/ms3/service/contract/EmpleadoInventarioPermisoService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.empleado.filter.EmpleadoInventarioPermisoFilterDto;
import com.upsjb.ms3.dto.empleado.request.EmpleadoInventarioPermisoRevokeRequestDto;
import com.upsjb.ms3.dto.empleado.request.EmpleadoInventarioPermisoUpdateRequestDto;
import com.upsjb.ms3.dto.empleado.response.EmpleadoInventarioPermisoResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;

public interface EmpleadoInventarioPermisoService {

    ApiResponseDto<EmpleadoInventarioPermisoResponseDto> otorgarOActualizar(
            EmpleadoInventarioPermisoUpdateRequestDto request
    );

    ApiResponseDto<EmpleadoInventarioPermisoResponseDto> revocar(
            Long idPermisoHistorial,
            EmpleadoInventarioPermisoRevokeRequestDto request
    );

    ApiResponseDto<EmpleadoInventarioPermisoResponseDto> obtenerDetalle(Long idPermisoHistorial);

    ApiResponseDto<EmpleadoInventarioPermisoResponseDto> obtenerVigentePorUsuarioMs1(Long idUsuarioMs1);

    ApiResponseDto<PageResponseDto<EmpleadoInventarioPermisoResponseDto>> listar(
            EmpleadoInventarioPermisoFilterDto filter,
            PageRequestDto pageRequest
    );

    boolean tienePermisoVigente(Long idUsuarioMs1);

    boolean puedeCrearProductoBasico(Long idUsuarioMs1);

    boolean puedeEditarProductoBasico(Long idUsuarioMs1);

    boolean puedeRegistrarEntrada(Long idUsuarioMs1);

    boolean puedeRegistrarSalida(Long idUsuarioMs1);

    boolean puedeRegistrarAjuste(Long idUsuarioMs1);

    boolean puedeConsultarKardex(Long idUsuarioMs1);

    boolean puedeGestionarImagenes(Long idUsuarioMs1);

    boolean puedeActualizarAtributos(Long idUsuarioMs1);
}