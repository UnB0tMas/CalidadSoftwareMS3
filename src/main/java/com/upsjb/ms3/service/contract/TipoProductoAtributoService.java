// ruta: src/main/java/com/upsjb/ms3/service/contract/TipoProductoAtributoService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.catalogo.atributo.filter.TipoProductoAtributoFilterDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.TipoProductoAtributoAssignRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.TipoProductoAtributoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.TipoProductoAtributoResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface TipoProductoAtributoService {

    ApiResponseDto<TipoProductoAtributoResponseDto> asignar(TipoProductoAtributoAssignRequestDto request);

    ApiResponseDto<TipoProductoAtributoResponseDto> actualizar(
            Long idTipoProductoAtributo,
            TipoProductoAtributoUpdateRequestDto request
    );

    ApiResponseDto<TipoProductoAtributoResponseDto> cambiarEstado(
            Long idTipoProductoAtributo,
            EstadoChangeRequestDto request
    );

    ApiResponseDto<TipoProductoAtributoResponseDto> obtenerDetalle(Long idTipoProductoAtributo);

    ApiResponseDto<PageResponseDto<TipoProductoAtributoResponseDto>> listar(
            TipoProductoAtributoFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<PageResponseDto<TipoProductoAtributoResponseDto>> listarPorTipoProducto(
            EntityReferenceDto tipoProducto,
            PageRequestDto pageRequest
    );

    ApiResponseDto<List<TipoProductoAtributoResponseDto>> obtenerPlantillaActiva(EntityReferenceDto tipoProducto);

    boolean existeAsociacionActiva(Long idTipoProducto, Long idAtributo);
}