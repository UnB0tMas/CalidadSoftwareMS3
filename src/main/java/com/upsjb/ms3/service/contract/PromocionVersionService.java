// ruta: src/main/java/com/upsjb/ms3/service/contract/PromocionVersionService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.promocion.filter.PromocionVersionFilterDto;
import com.upsjb.ms3.dto.promocion.request.PromocionVersionCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionVersionEstadoRequestDto;
import com.upsjb.ms3.dto.promocion.response.PromocionPublicResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionVersionResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface PromocionVersionService {

    ApiResponseDto<PromocionVersionResponseDto> crear(PromocionVersionCreateRequestDto request);

    ApiResponseDto<PromocionVersionResponseDto> cambiarEstado(
            Long idPromocionVersion,
            PromocionVersionEstadoRequestDto request
    );

    ApiResponseDto<PromocionVersionResponseDto> cancelar(
            Long idPromocionVersion,
            EstadoChangeRequestDto request
    );

    ApiResponseDto<PromocionVersionResponseDto> obtenerDetalle(Long idPromocionVersion);

    ApiResponseDto<PageResponseDto<PromocionVersionResponseDto>> listar(
            PromocionVersionFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<PageResponseDto<PromocionVersionResponseDto>> listarPorPromocion(
            EntityReferenceDto promocion,
            PageRequestDto pageRequest
    );

    ApiResponseDto<List<PromocionPublicResponseDto>> listarPublicasVigentes();
}