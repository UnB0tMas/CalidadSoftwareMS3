// ruta: src/main/java/com/upsjb/ms3/service/impl/ReferenceDataServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.CloudinaryResourceType;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.EstadoCompraInventario;
import com.upsjb.ms3.domain.enums.EstadoMovimientoInventario;
import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.domain.enums.EstadoPublicacionEvento;
import com.upsjb.ms3.domain.enums.EstadoRegistro;
import com.upsjb.ms3.domain.enums.EstadoReservaStock;
import com.upsjb.ms3.domain.enums.EstadoSku;
import com.upsjb.ms3.domain.enums.GeneroObjetivo;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.Ms4StockEventType;
import com.upsjb.ms3.domain.enums.PrecioEventType;
import com.upsjb.ms3.domain.enums.ProductoEventType;
import com.upsjb.ms3.domain.enums.PromocionEventType;
import com.upsjb.ms3.domain.enums.ResultadoAuditoria;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.StockEventType;
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

        data.put("estadosProductoRegistro", optionList(EstadoProductoRegistro.values()));
        data.put("estadosProductoPublicacion", optionList(EstadoProductoPublicacion.values()));
        data.put("estadosProductoVenta", optionList(EstadoProductoVenta.values()));
        data.put("estadosSku", optionList(EstadoSku.values()));
        data.put("generosObjetivo", optionList(GeneroObjetivo.values()));
        data.put("monedas", optionList(Moneda.values()));
        data.put("tiposProveedor", optionList(TipoProveedor.values()));
        data.put("tiposDocumentoProveedor", optionList(TipoDocumentoProveedor.values()));
        data.put("tiposDescuento", optionList(TipoDescuento.values()));
        data.put("estadosPromocion", optionList(EstadoPromocion.values()));
        data.put("estadosCompraInventario", optionList(EstadoCompraInventario.values()));
        data.put("estadosReservaStock", optionList(EstadoReservaStock.values()));
        data.put("tiposReferenciaStock", optionList(TipoReferenciaStock.values()));
        data.put("tiposMovimientoInventario", optionList(TipoMovimientoInventario.values()));
        data.put("motivosMovimientoInventario", optionList(MotivoMovimientoInventario.values()));
        data.put("estadosMovimientoInventario", optionList(EstadoMovimientoInventario.values()));
        data.put("estadosPublicacionEvento", optionList(EstadoPublicacionEvento.values()));
        data.put("tiposDatoAtributo", optionList(TipoDatoAtributo.values()));
        data.put("rolesSistema", optionList(RolSistema.values()));
        data.put("resultadosAuditoria", optionList(ResultadoAuditoria.values()));
        data.put("entidadesAuditadas", optionList(EntidadAuditada.values()));
        data.put("tiposEventoAuditoria", optionList(TipoEventoAuditoria.values()));
        data.put("aggregateTypes", optionList(AggregateType.values()));
        data.put("estadosRegistro", optionList(EstadoRegistro.values()));
        data.put("cloudinaryResourceTypes", optionList(CloudinaryResourceType.values()));
        data.put("productoEventTypes", optionList(ProductoEventType.values()));
        data.put("precioEventTypes", optionList(PrecioEventType.values()));
        data.put("promocionEventTypes", optionList(PromocionEventType.values()));
        data.put("stockEventTypes", optionList(StockEventType.values()));
        data.put("ms4StockEventTypes", optionList(Ms4StockEventType.values()));

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosProductoRegistro() {
        return ok(optionList(EstadoProductoRegistro.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosProductoPublicacion() {
        return ok(optionList(EstadoProductoPublicacion.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosProductoVenta() {
        return ok(optionList(EstadoProductoVenta.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosSku() {
        return ok(optionList(EstadoSku.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> generosObjetivo() {
        return ok(optionList(GeneroObjetivo.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> monedas() {
        return ok(optionList(Moneda.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> tiposProveedor() {
        return ok(optionList(TipoProveedor.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> tiposDocumentoProveedor() {
        return ok(optionList(TipoDocumentoProveedor.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> tiposDescuento() {
        return ok(optionList(TipoDescuento.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosPromocion() {
        return ok(optionList(EstadoPromocion.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosCompraInventario() {
        return ok(optionList(EstadoCompraInventario.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosReservaStock() {
        return ok(optionList(EstadoReservaStock.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> tiposReferenciaStock() {
        return ok(optionList(TipoReferenciaStock.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> tiposMovimientoInventario() {
        return ok(optionList(TipoMovimientoInventario.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> motivosMovimientoInventario() {
        return ok(optionList(MotivoMovimientoInventario.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosMovimientoInventario() {
        return ok(optionList(EstadoMovimientoInventario.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosPublicacionEvento() {
        return ok(optionList(EstadoPublicacionEvento.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> tiposDatoAtributo() {
        return ok(optionList(TipoDatoAtributo.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> rolesSistema() {
        return ok(optionList(RolSistema.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> resultadosAuditoria() {
        return ok(optionList(ResultadoAuditoria.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> entidadesAuditadas() {
        return ok(optionList(EntidadAuditada.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> tiposEventoAuditoria() {
        return ok(optionList(TipoEventoAuditoria.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> aggregateTypes() {
        return ok(optionList(AggregateType.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> estadosRegistro() {
        return ok(optionList(EstadoRegistro.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> cloudinaryResourceTypes() {
        return ok(optionList(CloudinaryResourceType.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> productoEventTypes() {
        return ok(optionList(ProductoEventType.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> precioEventTypes() {
        return ok(optionList(PrecioEventType.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> promocionEventTypes() {
        return ok(optionList(PromocionEventType.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> stockEventTypes() {
        return ok(optionList(StockEventType.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<SelectOptionDto>> ms4StockEventTypes() {
        return ok(optionList(Ms4StockEventType.values()));
    }

    private ApiResponseDto<List<SelectOptionDto>> ok(List<SelectOptionDto> data) {
        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", data);
    }

    private <E extends Enum<E>> List<SelectOptionDto> optionList(E[] values) {
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