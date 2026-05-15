// ruta: src/main/java/com/upsjb/ms3/service/contract/StockService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.inventario.stock.filter.StockSkuFilterDto;
import com.upsjb.ms3.dto.inventario.stock.response.StockDisponibleResponseDto;
import com.upsjb.ms3.dto.inventario.stock.response.StockSkuDetailResponseDto;
import com.upsjb.ms3.dto.inventario.stock.response.StockSkuResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;

public interface StockService {

    ApiResponseDto<PageResponseDto<StockSkuResponseDto>> listar(
            StockSkuFilterDto filter,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    );

    ApiResponseDto<StockSkuDetailResponseDto> obtenerDetalle(
            Long idStock,
            Boolean incluirCostos
    );

    ApiResponseDto<StockSkuDetailResponseDto> obtenerPorSkuYAlmacen(
            EntityReferenceDto sku,
            EntityReferenceDto almacen,
            Boolean incluirCostos
    );

    ApiResponseDto<StockDisponibleResponseDto> consultarDisponible(
            EntityReferenceDto sku,
            EntityReferenceDto almacen,
            Integer cantidadSolicitada
    );

    ApiResponseDto<PageResponseDto<StockSkuResponseDto>> listarPorSku(
            EntityReferenceDto sku,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    );

    ApiResponseDto<PageResponseDto<StockSkuResponseDto>> listarPorAlmacen(
            EntityReferenceDto almacen,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    );

    ApiResponseDto<PageResponseDto<StockSkuResponseDto>> listarBajoStock(
            PageRequestDto pageRequest,
            Boolean incluirCostos
    );

    Long obtenerStockDisponibleTotalPorSku(Long idSku);

    Long obtenerStockDisponibleTotalPorProducto(Long idProducto);

    boolean existeStockDisponible(Long idSku, Long idAlmacen, Integer cantidad);
}