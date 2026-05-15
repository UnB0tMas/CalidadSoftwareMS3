// ruta: src/main/java/com/upsjb/ms3/service/contract/PromocionSkuDescuentoService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.promocion.filter.PromocionSkuDescuentoFilterDto;
import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoUpdateRequestDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoCalculoResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface PromocionSkuDescuentoService {

    ApiResponseDto<PromocionSkuDescuentoResponseDto> agregar(
            Long idPromocionVersion,
            PromocionSkuDescuentoCreateRequestDto request
    );

    ApiResponseDto<PromocionSkuDescuentoCalculoResponseDto> calcular(
            Long idPromocionVersion,
            PromocionSkuDescuentoCreateRequestDto request
    );

    ApiResponseDto<PromocionSkuDescuentoResponseDto> actualizar(
            Long idPromocionSkuDescuentoVersion,
            PromocionSkuDescuentoUpdateRequestDto request
    );

    ApiResponseDto<PromocionSkuDescuentoResponseDto> inactivar(
            Long idPromocionSkuDescuentoVersion,
            EstadoChangeRequestDto request
    );

    ApiResponseDto<PromocionSkuDescuentoResponseDto> obtenerDetalle(Long idPromocionSkuDescuentoVersion);

    ApiResponseDto<PageResponseDto<PromocionSkuDescuentoResponseDto>> listar(
            PromocionSkuDescuentoFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<PageResponseDto<PromocionSkuDescuentoResponseDto>> listarPorVersion(
            Long idPromocionVersion,
            PageRequestDto pageRequest
    );

    ApiResponseDto<PageResponseDto<PromocionSkuDescuentoResponseDto>> listarPorSku(
            EntityReferenceDto sku,
            PageRequestDto pageRequest
    );

    ApiResponseDto<List<PromocionSkuDescuentoResponseDto>> listarAplicablesPorSku(EntityReferenceDto sku);
}