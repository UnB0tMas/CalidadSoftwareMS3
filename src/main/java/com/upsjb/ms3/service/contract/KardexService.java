// ruta: src/main/java/com/upsjb/ms3/service/contract/KardexService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.inventario.movimiento.filter.KardexFilterDto;
import com.upsjb.ms3.dto.inventario.movimiento.response.KardexResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;

public interface KardexService {

    ApiResponseDto<PageResponseDto<KardexResponseDto>> consultar(
            KardexFilterDto filter,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    );

    ApiResponseDto<KardexResponseDto> obtenerMovimiento(
            Long idMovimiento,
            Boolean incluirCostos
    );

    ApiResponseDto<KardexResponseDto> obtenerMovimientoPorCodigo(
            String codigoMovimiento,
            Boolean incluirCostos
    );

    ApiResponseDto<PageResponseDto<KardexResponseDto>> consultarPorSku(
            Long idSku,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    );

    ApiResponseDto<PageResponseDto<KardexResponseDto>> consultarPorSkuReferencia(
            EntityReferenceDto skuReference,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    );

    ApiResponseDto<PageResponseDto<KardexResponseDto>> consultarPorAlmacen(
            Long idAlmacen,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    );

    ApiResponseDto<PageResponseDto<KardexResponseDto>> consultarPorAlmacenReferencia(
            EntityReferenceDto almacenReference,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    );

    ApiResponseDto<PageResponseDto<KardexResponseDto>> consultarPorSkuYAlmacen(
            EntityReferenceDto skuReference,
            EntityReferenceDto almacenReference,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    );

    ApiResponseDto<PageResponseDto<KardexResponseDto>> consultarPorReferencia(
            String referenciaTipo,
            String referenciaIdExterno,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    );
}