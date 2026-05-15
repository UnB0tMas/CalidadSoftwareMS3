// ruta: src/main/java/com/upsjb/ms3/service/contract/ProductoAdminService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoEstadoRegistroRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoPublicacionRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoVentaEstadoRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;

public interface ProductoAdminService {

    ApiResponseDto<ProductoResponseDto> crear(ProductoCreateRequestDto request);

    ApiResponseDto<ProductoResponseDto> actualizar(Long idProducto, ProductoUpdateRequestDto request);

    ApiResponseDto<ProductoResponseDto> cambiarEstadoRegistro(
            Long idProducto,
            ProductoEstadoRegistroRequestDto request
    );

    ApiResponseDto<ProductoResponseDto> publicar(
            Long idProducto,
            ProductoPublicacionRequestDto request
    );

    ApiResponseDto<ProductoResponseDto> cambiarEstadoVenta(
            Long idProducto,
            ProductoVentaEstadoRequestDto request
    );

    ApiResponseDto<ProductoResponseDto> inactivar(
            Long idProducto,
            EstadoChangeRequestDto request
    );

    ApiResponseDto<ProductoResponseDto> obtenerPorId(Long idProducto);

    ApiResponseDto<ProductoDetailResponseDto> obtenerDetalle(Long idProducto);

    ApiResponseDto<PageResponseDto<ProductoResponseDto>> listar(
            ProductoFilterDto filter,
            PageRequestDto pageRequest
    );
}