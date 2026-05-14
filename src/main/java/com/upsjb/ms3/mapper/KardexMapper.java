// ruta: src/main/java/com/upsjb/ms3/mapper/KardexMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.MovimientoInventario;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.inventario.movimiento.response.KardexResponseDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class KardexMapper {

    public KardexResponseDto toResponse(MovimientoInventario entity) {
        return toResponse(entity, Moneda.PEN, true);
    }

    public KardexResponseDto toResponse(MovimientoInventario entity, Boolean includeCosts) {
        return toResponse(entity, Moneda.PEN, Boolean.TRUE.equals(includeCosts));
    }

    public KardexResponseDto toResponse(MovimientoInventario entity, Moneda moneda) {
        return toResponse(entity, moneda, true);
    }

    public KardexResponseDto toResponse(
            MovimientoInventario entity,
            Moneda moneda,
            boolean includeCosts
    ) {
        if (entity == null) {
            return null;
        }

        ProductoSku sku = entity.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        Almacen almacen = entity.getAlmacen();
        TipoMovimientoInventario tipoMovimiento = entity.getTipoMovimiento();

        return KardexResponseDto.builder()
                .idMovimiento(entity.getIdMovimiento())
                .fechaMovimiento(entity.getCreatedAt())
                .codigoMovimiento(entity.getCodigoMovimiento())
                .codigoGenerado(entity.getCodigoGenerado())
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .idAlmacen(almacen == null ? null : almacen.getIdAlmacen())
                .codigoAlmacen(almacen == null ? null : almacen.getCodigo())
                .nombreAlmacen(almacen == null ? null : almacen.getNombre())
                .idCompraDetalle(entity.getCompraDetalle() == null ? null : entity.getCompraDetalle().getIdCompraDetalle())
                .idReservaStock(entity.getReservaStock() == null ? null : entity.getReservaStock().getIdReservaStock())
                .tipoMovimiento(tipoMovimiento)
                .motivoMovimiento(entity.getMotivoMovimiento())
                .entrada(resolveEntrada(tipoMovimiento, entity.getCantidad()))
                .salida(resolveSalida(tipoMovimiento, entity.getCantidad()))
                .cantidad(defaultInteger(entity.getCantidad()))
                .stockAnterior(defaultInteger(entity.getStockAnterior()))
                .stockNuevo(defaultInteger(entity.getStockNuevo()))
                .variacionStock(resolveVariacion(entity))
                .costoUnitario(includeCosts ? toMoney(entity.getCostoUnitario(), moneda) : null)
                .costoTotal(includeCosts ? toMoney(entity.getCostoTotal(), moneda) : null)
                .referenciaTipo(entity.getReferenciaTipo())
                .referenciaIdExterno(entity.getReferenciaIdExterno())
                .observacion(entity.getObservacion())
                .actorIdUsuarioMs1(entity.getActorIdUsuarioMs1())
                .actorIdEmpleadoMs2(entity.getActorIdEmpleadoMs2())
                .actorRol(entity.getActorRol())
                .estadoMovimiento(entity.getEstadoMovimiento())
                .requestId(entity.getRequestId())
                .correlationId(entity.getCorrelationId())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<KardexResponseDto> toResponseList(List<MovimientoInventario> entities) {
        return toResponseList(entities, Moneda.PEN, true);
    }

    public List<KardexResponseDto> toResponseList(
            List<MovimientoInventario> entities,
            Moneda moneda,
            boolean includeCosts
    ) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(entity -> toResponse(entity, moneda, includeCosts))
                .toList();
    }

    private Integer resolveEntrada(TipoMovimientoInventario tipoMovimiento, Integer cantidad) {
        if (tipoMovimiento == null || !tipoMovimiento.isEntradaFisica()) {
            return 0;
        }

        return defaultInteger(cantidad);
    }

    private Integer resolveSalida(TipoMovimientoInventario tipoMovimiento, Integer cantidad) {
        if (tipoMovimiento == null || !tipoMovimiento.isSalidaFisica()) {
            return 0;
        }

        return defaultInteger(cantidad);
    }

    private Integer resolveVariacion(MovimientoInventario entity) {
        if (entity == null || entity.getStockAnterior() == null || entity.getStockNuevo() == null) {
            return null;
        }

        return entity.getStockNuevo() - entity.getStockAnterior();
    }

    private MoneyResponseDto toMoney(BigDecimal amount, Moneda moneda) {
        if (amount == null || moneda == null) {
            return null;
        }

        return MoneyResponseDto.builder()
                .amount(amount)
                .currency(moneda.getCode())
                .formatted(moneda.getSymbol() + " " + amount)
                .build();
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }
}