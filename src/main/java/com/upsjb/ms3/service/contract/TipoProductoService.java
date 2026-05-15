// ruta: src/main/java/com/upsjb/ms3/service/contract/TipoProductoService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.catalogo.tipoproducto.filter.TipoProductoFilterDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.request.TipoProductoCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.request.TipoProductoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.response.TipoProductoDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.response.TipoProductoResponseDto;
import com.upsjb.ms3.dto.reference.response.TipoProductoOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface TipoProductoService {

    ApiResponseDto<TipoProductoResponseDto> crear(TipoProductoCreateRequestDto request);

    ApiResponseDto<TipoProductoResponseDto> actualizar(Long idTipoProducto, TipoProductoUpdateRequestDto request);

    ApiResponseDto<TipoProductoResponseDto> cambiarEstado(Long idTipoProducto, EstadoChangeRequestDto request);

    ApiResponseDto<TipoProductoResponseDto> obtenerPorId(Long idTipoProducto);

    ApiResponseDto<TipoProductoDetailResponseDto> obtenerDetalle(Long idTipoProducto);

    ApiResponseDto<PageResponseDto<TipoProductoResponseDto>> listar(
            TipoProductoFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<List<TipoProductoOptionDto>> lookup(String search, Integer limit);
}