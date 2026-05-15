// ruta: src/main/java/com/upsjb/ms3/service/contract/SkuAtributoValorService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.catalogo.producto.filter.SkuAtributoValorFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.request.SkuAtributoValorRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.SkuAtributoValorResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.List;

public interface SkuAtributoValorService {

    ApiResponseDto<SkuAtributoValorResponseDto> guardarValor(
            Long idSku,
            SkuAtributoValorRequestDto request
    );

    ApiResponseDto<SkuAtributoValorResponseDto> guardarValor(
            EntityReferenceDto skuReference,
            SkuAtributoValorRequestDto request
    );

    ApiResponseDto<List<SkuAtributoValorResponseDto>> reemplazarValores(
            Long idSku,
            List<SkuAtributoValorRequestDto> request
    );

    ApiResponseDto<List<SkuAtributoValorResponseDto>> reemplazarValores(
            EntityReferenceDto skuReference,
            List<SkuAtributoValorRequestDto> request
    );

    ApiResponseDto<List<SkuAtributoValorResponseDto>> listarPorSku(Long idSku);

    ApiResponseDto<List<SkuAtributoValorResponseDto>> listarPorSku(EntityReferenceDto skuReference);

    ApiResponseDto<PageResponseDto<SkuAtributoValorResponseDto>> listar(
            SkuAtributoValorFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<SkuAtributoValorResponseDto> obtenerDetalle(Long idSkuAtributoValor);

    ApiResponseDto<SkuAtributoValorResponseDto> inactivar(
            Long idSkuAtributoValor,
            EstadoChangeRequestDto request
    );
}