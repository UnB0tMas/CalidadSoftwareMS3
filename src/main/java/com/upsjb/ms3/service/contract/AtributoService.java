// ruta: src/main/java/com/upsjb/ms3/service/contract/AtributoService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.dto.catalogo.atributo.filter.AtributoFilterDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.AtributoCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.AtributoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.AtributoDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.AtributoResponseDto;
import com.upsjb.ms3.dto.reference.response.AtributoOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface AtributoService {

    ApiResponseDto<AtributoResponseDto> crear(AtributoCreateRequestDto request);

    ApiResponseDto<AtributoResponseDto> actualizar(Long idAtributo, AtributoUpdateRequestDto request);

    ApiResponseDto<AtributoResponseDto> cambiarEstado(Long idAtributo, EstadoChangeRequestDto request);

    ApiResponseDto<AtributoResponseDto> obtenerPorId(Long idAtributo);

    ApiResponseDto<AtributoDetailResponseDto> obtenerDetalle(Long idAtributo);

    ApiResponseDto<PageResponseDto<AtributoResponseDto>> listar(
            AtributoFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<List<AtributoOptionDto>> lookup(String search, Integer limit);

    ApiResponseDto<List<AtributoResponseDto>> listarFiltrables();

    ApiResponseDto<List<AtributoResponseDto>> listarVisiblesPublico();

    ApiResponseDto<List<AtributoResponseDto>> listarPorTipoDato(TipoDatoAtributo tipoDato);
}