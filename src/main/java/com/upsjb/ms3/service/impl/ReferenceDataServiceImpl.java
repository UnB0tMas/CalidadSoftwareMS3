// ruta: src/main/java/com/upsjb/ms3/service/impl/ReferenceDataServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.EstadoCompraInventario;
import com.upsjb.ms3.domain.enums.EstadoMovimientoInventario;
import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.domain.enums.EstadoPublicacionEvento;
import com.upsjb.ms3.domain.enums.EstadoReservaStock;
import com.upsjb.ms3.domain.enums.EstadoSku;
import com.upsjb.ms3.domain.enums.GeneroObjetivo;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.ResultadoAuditoria;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.domain.enums.TipoDescuento;
import com.upsjb.ms3.domain.enums.TipoDocumentoProveedor;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.domain.enums.TipoProveedor;
import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.SelectOptionDto;
import com.upsjb.ms3.service.contract.ReferenceDataService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReferenceDataServiceImpl implements ReferenceDataService {

    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<Map<String, List<SelectOptionDto>>> listarTodo() {
        Map<String, List<SelectOptionDto>> data = new LinkedHashMap<>();

        data.put("estadosProductoRegistro", options(EstadoProductoRegistro.values()));
        data.put("estadosProductoPublicacion", options(EstadoProductoPublicacion.values()));
        data.put("estadosProductoVenta", options(EstadoProductoVenta.values()));
        data.put("estadosSku", options(EstadoSku.values()));
        data.put("generosObjetivo", options(GeneroObjetivo.values()));
        data.put("monedas", options(Moneda.values()));
        data.put("tiposProveedor", options(TipoProveedor.values()));
        data.put("tiposDocumentoProveedor", options(TipoDocumentoProveedor.values()));
        data.put("tiposDescuento", options(TipoDescuento.values()));
        data.put("estadosPromocion", options(EstadoPromocion.values()));
        data.put("estadosCompraInventario", options(EstadoCompraInventario.values()));
        data.put("estadosReservaStock", options(EstadoReservaStock.values()));
        data.put("tiposReferenciaStock", options(TipoReferenciaStock.values()));
        data.put("tiposMovimientoInventario", options(TipoMovimientoInventario.values()));
        data.put("motivosMovimientoInventario", options(MotivoMovimientoInventario.values()));
        data.put("estadosMovimientoInventario", options(EstadoMovimientoInventario.values()));
        data.put("estadosPublicacionEvento", options(EstadoPublicacionEvento.values()));
        data.put("tiposDatoAtributo", options(TipoDatoAtributo.values()));
        data.put("rolesSistema", options(RolSistema.values()));
        data.put("resultadosAuditoria", options(ResultadoAuditoria.values()));
        data.put("entidadesAuditadas", options(EntidadAuditada.values()));
        data.put("tiposEventoAuditoria", options(TipoEventoAuditoria.values()));
        data.put("aggregateTypes", options(AggregateType.values()));

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                data
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosProductoRegistro() {
        return ok(options(EstadoProductoRegistro.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosProductoPublicacion() {
        return ok(options(EstadoProductoPublicacion.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosProductoVenta() {
        return ok(options(EstadoProductoVenta.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosSku() {
        return ok(options(EstadoSku.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> generosObjetivo() {
        return ok(options(GeneroObjetivo.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> monedas() {
        return ok(options(Moneda.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> tiposProveedor() {
        return ok(options(TipoProveedor.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> tiposDocumentoProveedor() {
        return ok(options(TipoDocumentoProveedor.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> tiposDescuento() {
        return ok(options(TipoDescuento.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosPromocion() {
        return ok(options(EstadoPromocion.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosCompraInventario() {
        return ok(options(EstadoCompraInventario.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosReservaStock() {
        return ok(options(EstadoReservaStock.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> tiposReferenciaStock() {
        return ok(options(TipoReferenciaStock.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> tiposMovimientoInventario() {
        return ok(options(TipoMovimientoInventario.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> motivosMovimientoInventario() {
        return ok(options(MotivoMovimientoInventario.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosMovimientoInventario() {
        return ok(options(EstadoMovimientoInventario.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosPublicacionEvento() {
        return ok(options(EstadoPublicacionEvento.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> tiposDatoAtributo() {
        return ok(options(TipoDatoAtributo.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> rolesSistema() {
        return ok(options(RolSistema.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> resultadosAuditoria() {
        return ok(options(ResultadoAuditoria.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> entidadesAuditadas() {
        return ok(options(EntidadAuditada.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> tiposEventoAuditoria() {
        return ok(options(TipoEventoAuditoria.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> aggregateTypes() {
        return ok(options(AggregateType.values()));
    }

    private ApiResponseDto<List<SelectOptionDto>> ok(List<SelectOptionDto> data) {
        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                data
        );
    }

    private <E extends Enum<E>> List<SelectOptionDto> options(E[] values) {
        return Arrays.stream(values)
                .map(this::toOption)
                .toList();
    }

    private SelectOptionDto toOption(Enum<?> value) {
        String code = methodValue(value, "getCode", value.name());
        String label = methodValue(value, "getLabel", value.name());

        return SelectOptionDto.builder()
                .value(code)
                .code(code)
                .label(label)
                .disabled(Boolean.FALSE)
                .build();
    }

    private String methodValue(Enum<?> value, String methodName, String fallback) {
        try {
            Method method = value.getClass().getMethod(methodName);
            Object result = method.invoke(value);
            return result == null ? fallback : String.valueOf(result);
        } catch (ReflectiveOperationException ex) {
            return fallback;
        }
    }
}