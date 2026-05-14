// ruta: src/main/java/com/upsjb/ms3/service/contract/EmpleadoSnapshotMs2Service.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.empleado.filter.EmpleadoSnapshotMs2FilterDto;
import com.upsjb.ms3.dto.empleado.request.EmpleadoSnapshotMs2UpsertRequestDto;
import com.upsjb.ms3.dto.empleado.response.EmpleadoSnapshotMs2ResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;

public interface EmpleadoSnapshotMs2Service {

    ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> upsert(EmpleadoSnapshotMs2UpsertRequestDto request);

    ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> sincronizarDesdeMs2PorUsuarioMs1(Long idUsuarioMs1);

    ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> sincronizarDesdeMs2PorEmpleadoMs2(Long idEmpleadoMs2);

    ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> obtenerPorId(Long idEmpleadoSnapshot);

    ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> obtenerPorIdUsuarioMs1(Long idUsuarioMs1);

    ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> obtenerPorIdEmpleadoMs2(Long idEmpleadoMs2);

    ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> obtenerPorCodigoEmpleado(String codigoEmpleado);

    ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> validarEmpleadoActivoPorUsuarioMs1(Long idUsuarioMs1);

    ApiResponseDto<PageResponseDto<EmpleadoSnapshotMs2ResponseDto>> listar(
            EmpleadoSnapshotMs2FilterDto filter,
            PageRequestDto pageRequest
    );

    boolean existeEmpleadoActivoPorUsuarioMs1(Long idUsuarioMs1);

    boolean existeEmpleadoActivoPorEmpleadoMs2(Long idEmpleadoMs2);
}