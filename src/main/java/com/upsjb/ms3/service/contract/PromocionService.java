// ruta: src/main/java/com/upsjb/ms3/service/contract/PromocionService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.promocion.filter.PromocionFilterDto;
import com.upsjb.ms3.dto.promocion.filter.PromocionVersionFilterDto;
import com.upsjb.ms3.dto.promocion.request.PromocionCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoUpdateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionUpdateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionVersionCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionVersionEstadoRequestDto;
import com.upsjb.ms3.dto.promocion.response.PromocionDetailResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionPublicResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionVersionResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface PromocionService {

    ApiResponseDto<PromocionResponseDto> crear(PromocionCreateRequestDto request);

    ApiResponseDto<PromocionResponseDto> actualizar(Long idPromocion, PromocionUpdateRequestDto request);

    ApiResponseDto<PromocionResponseDto> inactivar(Long idPromocion, EstadoChangeRequestDto request);

    ApiResponseDto<PromocionResponseDto> obtenerPorId(Long idPromocion);

    ApiResponseDto<PromocionDetailResponseDto> obtenerDetalle(Long idPromocion);

    ApiResponseDto<PageResponseDto<PromocionResponseDto>> listar(
            PromocionFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<PromocionVersionResponseDto> crearVersion(PromocionVersionCreateRequestDto request);

    ApiResponseDto<PromocionVersionResponseDto> cambiarEstadoVersion(
            Long idPromocionVersion,
            PromocionVersionEstadoRequestDto request
    );

    ApiResponseDto<PromocionVersionResponseDto> cancelarVersion(
            Long idPromocionVersion,
            EstadoChangeRequestDto request
    );

    ApiResponseDto<PromocionVersionResponseDto> obtenerVersionDetalle(Long idPromocionVersion);

    ApiResponseDto<PageResponseDto<PromocionVersionResponseDto>> listarVersiones(
            PromocionVersionFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<PromocionSkuDescuentoResponseDto> agregarDescuentoSku(
            Long idPromocionVersion,
            PromocionSkuDescuentoCreateRequestDto request
    );

    ApiResponseDto<PromocionSkuDescuentoResponseDto> actualizarDescuentoSku(
            Long idPromocionSkuDescuentoVersion,
            PromocionSkuDescuentoUpdateRequestDto request
    );

    ApiResponseDto<PromocionSkuDescuentoResponseDto> inactivarDescuentoSku(
            Long idPromocionSkuDescuentoVersion,
            EstadoChangeRequestDto request
    );

    ApiResponseDto<List<PromocionPublicResponseDto>> listarPublicasVigentes();
}