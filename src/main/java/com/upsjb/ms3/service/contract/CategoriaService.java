// ruta: src/main/java/com/upsjb/ms3/service/contract/CategoriaService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.catalogo.categoria.filter.CategoriaFilterDto;
import com.upsjb.ms3.dto.catalogo.categoria.request.CategoriaCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.categoria.request.CategoriaUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaResponseDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaTreeResponseDto;
import com.upsjb.ms3.dto.reference.response.CategoriaOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface CategoriaService {

    ApiResponseDto<CategoriaResponseDto> crear(CategoriaCreateRequestDto request);

    ApiResponseDto<CategoriaResponseDto> actualizar(Long idCategoria, CategoriaUpdateRequestDto request);

    ApiResponseDto<CategoriaResponseDto> cambiarEstado(Long idCategoria, EstadoChangeRequestDto request);

    ApiResponseDto<CategoriaResponseDto> obtenerPorId(Long idCategoria);

    ApiResponseDto<CategoriaDetailResponseDto> obtenerDetalle(Long idCategoria);

    ApiResponseDto<PageResponseDto<CategoriaResponseDto>> listar(
            CategoriaFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<List<CategoriaOptionDto>> lookup(String search, Integer limit);

    ApiResponseDto<List<CategoriaTreeResponseDto>> obtenerArbol(Boolean soloActivas);

    ApiResponseDto<List<CategoriaResponseDto>> listarSubcategorias(Long idCategoriaPadre);
}