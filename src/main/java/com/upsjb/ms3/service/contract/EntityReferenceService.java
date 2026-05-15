// ruta: src/main/java/com/upsjb/ms3/service/contract/EntityReferenceService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.reference.filter.ReferenceSearchFilterDto;
import com.upsjb.ms3.dto.reference.request.EntityReferenceRequestDto;
import com.upsjb.ms3.dto.reference.response.EntityReferenceResolvedDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import java.util.List;

public interface EntityReferenceService {

    ApiResponseDto<EntityReferenceResolvedDto> resolver(EntityReferenceRequestDto request);

    ApiResponseDto<List<EntityReferenceResolvedDto>> buscar(String entidad, ReferenceSearchFilterDto filter);

    ApiResponseDto<List<EntityReferenceResolvedDto>> buscarTipoProducto(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<EntityReferenceResolvedDto>> buscarCategoria(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<EntityReferenceResolvedDto>> buscarMarca(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<EntityReferenceResolvedDto>> buscarAtributo(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<EntityReferenceResolvedDto>> buscarProducto(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<EntityReferenceResolvedDto>> buscarSku(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<EntityReferenceResolvedDto>> buscarProveedor(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<EntityReferenceResolvedDto>> buscarAlmacen(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<EntityReferenceResolvedDto>> buscarPromocion(ReferenceSearchFilterDto filter);

    ApiResponseDto<List<EntityReferenceResolvedDto>> buscarEmpleadoInventario(ReferenceSearchFilterDto filter);

    ApiResponseDto<EntityReferenceDto> resolverTipoProducto(EntityReferenceDto reference);

    ApiResponseDto<EntityReferenceDto> resolverCategoria(EntityReferenceDto reference);

    ApiResponseDto<EntityReferenceDto> resolverMarca(EntityReferenceDto reference);

    ApiResponseDto<EntityReferenceDto> resolverAtributo(EntityReferenceDto reference);

    ApiResponseDto<EntityReferenceDto> resolverProducto(EntityReferenceDto reference);

    ApiResponseDto<EntityReferenceDto> resolverSku(EntityReferenceDto reference);

    ApiResponseDto<EntityReferenceDto> resolverProveedor(EntityReferenceDto reference);

    ApiResponseDto<EntityReferenceDto> resolverAlmacen(EntityReferenceDto reference);

    ApiResponseDto<EntityReferenceDto> resolverPromocion(EntityReferenceDto reference);

    ApiResponseDto<EntityReferenceDto> resolverEmpleadoInventario(EntityReferenceDto reference);
}