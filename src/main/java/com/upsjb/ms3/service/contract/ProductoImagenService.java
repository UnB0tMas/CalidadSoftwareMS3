// ruta: src/main/java/com/upsjb/ms3/service/contract/ProductoImagenService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoImagenFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoImagenPrincipalRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoImagenUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoImagenUploadRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoImagenResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface ProductoImagenService {

    ApiResponseDto<ProductoImagenResponseDto> subir(ProductoImagenUploadRequestDto request);

    ApiResponseDto<ProductoImagenResponseDto> actualizarMetadata(
            Long idImagen,
            ProductoImagenUpdateRequestDto request
    );

    ApiResponseDto<ProductoImagenResponseDto> marcarPrincipal(
            Long idImagen,
            ProductoImagenPrincipalRequestDto request
    );

    ApiResponseDto<ProductoImagenResponseDto> inactivar(
            Long idImagen,
            EstadoChangeRequestDto request
    );

    ApiResponseDto<ProductoImagenResponseDto> obtenerDetalle(Long idImagen);

    ApiResponseDto<PageResponseDto<ProductoImagenResponseDto>> listar(
            ProductoImagenFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<List<ProductoImagenResponseDto>> listarPorProducto(EntityReferenceDto productoReference);

    ApiResponseDto<List<ProductoImagenResponseDto>> listarPorSku(EntityReferenceDto skuReference);

    ApiResponseDto<ProductoImagenResponseDto> obtenerPrincipalProducto(EntityReferenceDto productoReference);

    ApiResponseDto<ProductoImagenResponseDto> obtenerPrincipalSku(EntityReferenceDto skuReference);
}