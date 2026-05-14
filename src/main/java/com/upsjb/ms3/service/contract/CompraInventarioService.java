// ruta: src/main/java/com/upsjb/ms3/service/contract/CompraInventarioService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.inventario.compra.filter.CompraInventarioFilterDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioAnularRequestDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioConfirmRequestDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioCreateRequestDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioUpdateRequestDto;
import com.upsjb.ms3.dto.inventario.compra.response.CompraInventarioDetailResponseDto;
import com.upsjb.ms3.dto.inventario.compra.response.CompraInventarioResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;

public interface CompraInventarioService {

    ApiResponseDto<CompraInventarioDetailResponseDto> crear(CompraInventarioCreateRequestDto request);

    ApiResponseDto<CompraInventarioDetailResponseDto> actualizar(
            Long idCompra,
            CompraInventarioUpdateRequestDto request
    );

    ApiResponseDto<CompraInventarioDetailResponseDto> confirmar(
            Long idCompra,
            CompraInventarioConfirmRequestDto request
    );

    ApiResponseDto<CompraInventarioResponseDto> anular(
            Long idCompra,
            CompraInventarioAnularRequestDto request
    );

    ApiResponseDto<CompraInventarioResponseDto> obtenerPorId(Long idCompra);

    ApiResponseDto<CompraInventarioDetailResponseDto> obtenerDetalle(Long idCompra);

    ApiResponseDto<PageResponseDto<CompraInventarioResponseDto>> listar(
            CompraInventarioFilterDto filter,
            PageRequestDto pageRequest
    );
}