// ruta: src/main/java/com/upsjb/ms3/service/contract/MarcaService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.catalogo.marca.filter.MarcaFilterDto;
import com.upsjb.ms3.dto.catalogo.marca.request.MarcaCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.marca.request.MarcaUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.marca.response.MarcaDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.marca.response.MarcaResponseDto;
import com.upsjb.ms3.dto.reference.response.MarcaOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface MarcaService {

    ApiResponseDto<MarcaResponseDto> crear(MarcaCreateRequestDto request);

    ApiResponseDto<MarcaResponseDto> actualizar(Long idMarca, MarcaUpdateRequestDto request);

    ApiResponseDto<MarcaResponseDto> cambiarEstado(Long idMarca, EstadoChangeRequestDto request);

    ApiResponseDto<MarcaResponseDto> obtenerPorId(Long idMarca);

    ApiResponseDto<MarcaResponseDto> obtenerPorCodigo(String codigo);

    ApiResponseDto<MarcaResponseDto> obtenerPorSlug(String slug);

    ApiResponseDto<MarcaResponseDto> obtenerPorReferencia(EntityReferenceDto reference);

    ApiResponseDto<MarcaDetailResponseDto> obtenerDetalle(Long idMarca);

    ApiResponseDto<PageResponseDto<MarcaResponseDto>> listar(
            MarcaFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<List<MarcaOptionDto>> lookup(String search, Integer limit);
}