package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoBulkCreateRequestDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import java.util.List;

public interface PromocionSkuDescuentoBulkService {

    ApiResponseDto<
            List<
                    PromocionSkuDescuentoResponseDto
                    >
            > agregar(
            Long idPromocionVersion,
            PromocionSkuDescuentoBulkCreateRequestDto request
    );
}