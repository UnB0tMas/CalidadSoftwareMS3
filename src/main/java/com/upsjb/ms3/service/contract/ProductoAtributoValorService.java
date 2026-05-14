// ruta: src/main/java/com/upsjb/ms3/service/contract/ProductoAtributoValorService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.catalogo.producto.request.ProductoAtributoValorRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoAtributoValorResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import java.util.List;

public interface ProductoAtributoValorService {

    ApiResponseDto<ProductoAtributoValorResponseDto> guardarValor(
            Long idProducto,
            ProductoAtributoValorRequestDto request
    );

    ApiResponseDto<List<ProductoAtributoValorResponseDto>> reemplazarValores(
            Long idProducto,
            List<ProductoAtributoValorRequestDto> request
    );

    ApiResponseDto<List<ProductoAtributoValorResponseDto>> listarPorProducto(Long idProducto);

    ApiResponseDto<ProductoAtributoValorResponseDto> obtenerDetalle(Long idProductoAtributoValor);

    ApiResponseDto<ProductoAtributoValorResponseDto> inactivar(
            Long idProductoAtributoValor,
            EstadoChangeRequestDto request
    );
}