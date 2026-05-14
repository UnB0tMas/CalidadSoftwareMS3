// ruta: src/main/java/com/upsjb/ms3/service/contract/ProveedorService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.proveedor.filter.ProveedorFilterDto;
import com.upsjb.ms3.dto.proveedor.request.ProveedorCreateRequestDto;
import com.upsjb.ms3.dto.proveedor.request.ProveedorEstadoRequestDto;
import com.upsjb.ms3.dto.proveedor.request.ProveedorUpdateRequestDto;
import com.upsjb.ms3.dto.proveedor.response.ProveedorDetailResponseDto;
import com.upsjb.ms3.dto.proveedor.response.ProveedorResponseDto;
import com.upsjb.ms3.dto.reference.response.ProveedorOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface ProveedorService {

    ApiResponseDto<ProveedorResponseDto> crear(ProveedorCreateRequestDto request);

    ApiResponseDto<ProveedorResponseDto> actualizar(Long idProveedor, ProveedorUpdateRequestDto request);

    ApiResponseDto<ProveedorResponseDto> cambiarEstado(Long idProveedor, ProveedorEstadoRequestDto request);

    ApiResponseDto<ProveedorResponseDto> obtenerPorId(Long idProveedor);

    ApiResponseDto<ProveedorDetailResponseDto> obtenerDetalle(Long idProveedor);

    ApiResponseDto<PageResponseDto<ProveedorResponseDto>> listar(
            ProveedorFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<List<ProveedorOptionDto>> lookup(String search, Integer limit);

    ApiResponseDto<ProveedorResponseDto> obtenerPorRuc(String ruc);

    ApiResponseDto<ProveedorResponseDto> obtenerPorDocumento(String numeroDocumento);
}