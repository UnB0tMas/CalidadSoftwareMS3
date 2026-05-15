// ruta: src/main/java/com/upsjb/ms3/service/contract/ProductoSkuService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoSkuFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoSkuCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoSkuUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoSkuDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoSkuResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface ProductoSkuService {

    ApiResponseDto<ProductoSkuResponseDto> crear(ProductoSkuCreateRequestDto request);

    ApiResponseDto<ProductoSkuResponseDto> actualizar(Long idSku, ProductoSkuUpdateRequestDto request);

    ApiResponseDto<ProductoSkuResponseDto> inactivar(Long idSku, EstadoChangeRequestDto request);

    ApiResponseDto<ProductoSkuResponseDto> descontinuar(Long idSku, EstadoChangeRequestDto request);

    ApiResponseDto<ProductoSkuResponseDto> obtenerPorId(Long idSku);

    ApiResponseDto<ProductoSkuDetailResponseDto> obtenerDetalle(Long idSku);

    ApiResponseDto<PageResponseDto<ProductoSkuResponseDto>> listar(
            ProductoSkuFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<PageResponseDto<ProductoSkuResponseDto>> listarPorProducto(
            EntityReferenceDto productoReference,
            PageRequestDto pageRequest
    );

    ApiResponseDto<List<ProductoSkuResponseDto>> listarActivosPorProducto(EntityReferenceDto productoReference);
}