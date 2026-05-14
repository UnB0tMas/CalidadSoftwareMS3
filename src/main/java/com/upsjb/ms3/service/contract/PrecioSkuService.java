// ruta: src/main/java/com/upsjb/ms3/service/contract/PrecioSkuService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.precio.filter.PrecioSkuFilterDto;
import com.upsjb.ms3.dto.precio.request.PrecioSkuCreateRequestDto;
import com.upsjb.ms3.dto.precio.response.PrecioSkuHistorialResponseDto;
import com.upsjb.ms3.dto.precio.response.PrecioSkuResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;

public interface PrecioSkuService {

    ApiResponseDto<PrecioSkuResponseDto> registrarNuevoPrecio(PrecioSkuCreateRequestDto request);

    ApiResponseDto<PrecioSkuResponseDto> obtenerVigente(EntityReferenceDto skuReference);

    ApiResponseDto<PrecioSkuResponseDto> obtenerVigentePorSku(Long idSku);

    ApiResponseDto<PrecioSkuHistorialResponseDto> obtenerDetalle(Long idPrecioHistorial);

    ApiResponseDto<PageResponseDto<PrecioSkuHistorialResponseDto>> listarHistorial(
            PrecioSkuFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<PageResponseDto<PrecioSkuHistorialResponseDto>> listarHistorialPorSku(
            EntityReferenceDto skuReference,
            PageRequestDto pageRequest
    );
}