// ruta: src/main/java/com/upsjb/ms3/service/contract/ProductoAtributoValorService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoAtributoValorFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoAtributoValorRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoAtributoValorResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface ProductoAtributoValorService {

    ApiResponseDto<ProductoAtributoValorResponseDto> guardarValor(
            Long idProducto,
            ProductoAtributoValorRequestDto request
    );

    ApiResponseDto<ProductoAtributoValorResponseDto> guardarValor(
            EntityReferenceDto productoReference,
            ProductoAtributoValorRequestDto request
    );

    ApiResponseDto<List<ProductoAtributoValorResponseDto>> reemplazarValores(
            Long idProducto,
            List<ProductoAtributoValorRequestDto> request
    );

    ApiResponseDto<List<ProductoAtributoValorResponseDto>> reemplazarValores(
            EntityReferenceDto productoReference,
            List<ProductoAtributoValorRequestDto> request
    );

    ApiResponseDto<List<ProductoAtributoValorResponseDto>> listarPorProducto(Long idProducto);

    ApiResponseDto<List<ProductoAtributoValorResponseDto>> listarPorProducto(EntityReferenceDto productoReference);

    ApiResponseDto<PageResponseDto<ProductoAtributoValorResponseDto>> listar(
            ProductoAtributoValorFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<ProductoAtributoValorResponseDto> obtenerDetalle(Long idProductoAtributoValor);

    ApiResponseDto<ProductoAtributoValorResponseDto> inactivar(
            Long idProductoAtributoValor,
            EstadoChangeRequestDto request
    );
}