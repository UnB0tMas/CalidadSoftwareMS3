package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.catalogo.atributo.filter.CategoriaAtributoFilterDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.CategoriaAtributoAssignRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.CategoriaAtributoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.CategoriaAtributoResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface CategoriaAtributoService {

    ApiResponseDto<CategoriaAtributoResponseDto> asignar(
            CategoriaAtributoAssignRequestDto request
    );

    ApiResponseDto<CategoriaAtributoResponseDto> actualizar(
            Long idCategoriaAtributo,
            CategoriaAtributoUpdateRequestDto request
    );

    ApiResponseDto<CategoriaAtributoResponseDto> cambiarEstado(
            Long idCategoriaAtributo,
            EstadoChangeRequestDto request
    );

    ApiResponseDto<CategoriaAtributoResponseDto> obtenerDetalle(
            Long idCategoriaAtributo
    );

    ApiResponseDto<PageResponseDto<CategoriaAtributoResponseDto>> listar(
            CategoriaAtributoFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<PageResponseDto<CategoriaAtributoResponseDto>> listarPorCategoria(
            EntityReferenceDto categoria,
            PageRequestDto pageRequest
    );

    ApiResponseDto<List<CategoriaAtributoResponseDto>> obtenerPlantillaActiva(
            EntityReferenceDto categoria
    );

    boolean existeAsociacionActiva(
            Long idCategoria,
            Long idAtributo
    );
}