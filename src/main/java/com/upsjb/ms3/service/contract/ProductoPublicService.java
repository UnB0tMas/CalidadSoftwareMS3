// ruta: src/main/java/com/upsjb/ms3/service/contract/ProductoPublicService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoPublicFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoPublicDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoPublicResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;

public interface ProductoPublicService {

    ApiResponseDto<PageResponseDto<ProductoPublicResponseDto>> listar(
            ProductoPublicFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<ProductoPublicDetailResponseDto> obtenerDetallePorSlug(String slug);
}