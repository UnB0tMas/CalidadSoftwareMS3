// ruta: src/main/java/com/upsjb/ms3/service/contract/ReservaStockService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import com.upsjb.ms3.dto.inventario.reserva.filter.ReservaStockFilterDto;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockConfirmRequestDto;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockCreateRequestDto;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockLiberarRequestDto;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockMs4RequestDto;
import com.upsjb.ms3.dto.inventario.reserva.response.ReservaStockResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;

public interface ReservaStockService {

    ApiResponseDto<ReservaStockResponseDto> crear(ReservaStockCreateRequestDto request);

    ApiResponseDto<ReservaStockResponseDto> procesarReservaMs4(ReservaStockMs4RequestDto request);

    ApiResponseDto<ReservaStockResponseDto> confirmar(
            Long idReservaStock,
            ReservaStockConfirmRequestDto request
    );

    ApiResponseDto<ReservaStockResponseDto> confirmarPorCodigo(
            String codigoReserva,
            ReservaStockConfirmRequestDto request
    );

    ApiResponseDto<ReservaStockResponseDto> confirmarPorReferencia(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Long idSku,
            Long idAlmacen,
            ReservaStockConfirmRequestDto request
    );

    ApiResponseDto<ReservaStockResponseDto> confirmarPorReferencia(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            EntityReferenceDto sku,
            EntityReferenceDto almacen,
            ReservaStockConfirmRequestDto request
    );

    ApiResponseDto<ReservaStockResponseDto> liberar(
            Long idReservaStock,
            ReservaStockLiberarRequestDto request
    );

    ApiResponseDto<ReservaStockResponseDto> liberarPorCodigo(
            String codigoReserva,
            ReservaStockLiberarRequestDto request
    );

    ApiResponseDto<ReservaStockResponseDto> liberarPorReferencia(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Long idSku,
            Long idAlmacen,
            ReservaStockLiberarRequestDto request
    );

    ApiResponseDto<ReservaStockResponseDto> liberarPorReferencia(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            EntityReferenceDto sku,
            EntityReferenceDto almacen,
            ReservaStockLiberarRequestDto request
    );

    ApiResponseDto<ReservaStockResponseDto> vencer(Long idReservaStock);

    ApiResponseDto<ReservaStockResponseDto> obtenerPorId(Long idReservaStock);

    ApiResponseDto<ReservaStockResponseDto> obtenerPorCodigo(String codigoReserva);

    ApiResponseDto<ReservaStockResponseDto> obtenerPorReferencia(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Long idSku,
            Long idAlmacen
    );

    ApiResponseDto<ReservaStockResponseDto> obtenerPorReferencia(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            EntityReferenceDto sku,
            EntityReferenceDto almacen
    );

    ApiResponseDto<PageResponseDto<ReservaStockResponseDto>> listar(
            ReservaStockFilterDto filter,
            PageRequestDto pageRequest
    );
}