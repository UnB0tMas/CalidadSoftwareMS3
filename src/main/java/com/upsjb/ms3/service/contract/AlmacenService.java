// ruta: src/main/java/com/upsjb/ms3/service/contract/AlmacenService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.inventario.almacen.filter.AlmacenFilterDto;
import com.upsjb.ms3.dto.inventario.almacen.request.AlmacenCreateRequestDto;
import com.upsjb.ms3.dto.inventario.almacen.request.AlmacenEstadoRequestDto;
import com.upsjb.ms3.dto.inventario.almacen.request.AlmacenUpdateRequestDto;
import com.upsjb.ms3.dto.inventario.almacen.response.AlmacenDetailResponseDto;
import com.upsjb.ms3.dto.inventario.almacen.response.AlmacenResponseDto;
import com.upsjb.ms3.dto.reference.response.AlmacenOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface AlmacenService {

    ApiResponseDto<AlmacenResponseDto> crear(AlmacenCreateRequestDto request);

    ApiResponseDto<AlmacenResponseDto> actualizar(Long idAlmacen, AlmacenUpdateRequestDto request);

    ApiResponseDto<AlmacenResponseDto> cambiarEstado(Long idAlmacen, AlmacenEstadoRequestDto request);

    ApiResponseDto<AlmacenResponseDto> obtenerPorId(Long idAlmacen);

    ApiResponseDto<AlmacenDetailResponseDto> obtenerDetalle(Long idAlmacen);

    ApiResponseDto<PageResponseDto<AlmacenResponseDto>> listar(
            AlmacenFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<List<AlmacenOptionDto>> lookup(String search, Integer limit);

    ApiResponseDto<AlmacenResponseDto> obtenerPrincipal();

    ApiResponseDto<List<AlmacenResponseDto>> listarParaVenta();

    ApiResponseDto<List<AlmacenResponseDto>> listarParaCompra();
}