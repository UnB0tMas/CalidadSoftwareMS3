// ruta: src/main/java/com/upsjb/ms3/service/contract/ReferenceDataService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.SelectOptionDto;
import java.util.List;
import java.util.Map;

public interface ReferenceDataService {

    ApiResponseDto<Map<String, List<SelectOptionDto>>> listarTodo();

    ApiResponseDto<List<SelectOptionDto>> estadosProductoRegistro();

    ApiResponseDto<List<SelectOptionDto>> estadosProductoPublicacion();

    ApiResponseDto<List<SelectOptionDto>> estadosProductoVenta();

    ApiResponseDto<List<SelectOptionDto>> estadosSku();

    ApiResponseDto<List<SelectOptionDto>> generosObjetivo();

    ApiResponseDto<List<SelectOptionDto>> monedas();

    ApiResponseDto<List<SelectOptionDto>> tiposProveedor();

    ApiResponseDto<List<SelectOptionDto>> tiposDocumentoProveedor();

    ApiResponseDto<List<SelectOptionDto>> tiposDescuento();

    ApiResponseDto<List<SelectOptionDto>> estadosPromocion();

    ApiResponseDto<List<SelectOptionDto>> estadosCompraInventario();

    ApiResponseDto<List<SelectOptionDto>> estadosReservaStock();

    ApiResponseDto<List<SelectOptionDto>> tiposReferenciaStock();

    ApiResponseDto<List<SelectOptionDto>> tiposMovimientoInventario();

    ApiResponseDto<List<SelectOptionDto>> motivosMovimientoInventario();

    ApiResponseDto<List<SelectOptionDto>> estadosMovimientoInventario();

    ApiResponseDto<List<SelectOptionDto>> estadosPublicacionEvento();

    ApiResponseDto<List<SelectOptionDto>> tiposDatoAtributo();

    ApiResponseDto<List<SelectOptionDto>> rolesSistema();

    ApiResponseDto<List<SelectOptionDto>> resultadosAuditoria();

    ApiResponseDto<List<SelectOptionDto>> entidadesAuditadas();

    ApiResponseDto<List<SelectOptionDto>> tiposEventoAuditoria();

    ApiResponseDto<List<SelectOptionDto>> aggregateTypes();
}