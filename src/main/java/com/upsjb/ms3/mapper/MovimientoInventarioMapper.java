// ruta: src/main/java/com/upsjb/ms3/mapper/MovimientoInventarioMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.CompraInventarioDetalle;
import com.upsjb.ms3.domain.entity.MovimientoInventario;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.ReservaStock;
import com.upsjb.ms3.domain.enums.EstadoMovimientoInventario;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import com.upsjb.ms3.dto.inventario.movimiento.request.AjusteInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.EntradaInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.SalidaInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.response.KardexResponseDto;
import com.upsjb.ms3.dto.inventario.movimiento.response.MovimientoInventarioResponseDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class MovimientoInventarioMapper {

    public MovimientoInventario toEntity(
            String codigoMovimiento,
            ProductoSku sku,
            Almacen almacen,
            CompraInventarioDetalle compraDetalle,
            ReservaStock reservaStock,
            TipoMovimientoInventario tipoMovimiento,
            MotivoMovimientoInventario motivoMovimiento,
            Integer cantidad,
            BigDecimal costoUnitario,
            BigDecimal costoTotal,
            Integer stockAnterior,
            Integer stockNuevo,
            String referenciaTipo,
            String referenciaIdExterno,
            String observacion,
            Long actorIdUsuarioMs1,
            Long actorIdEmpleadoMs2,
            RolSistema actorRol,
            String requestId,
            String correlationId
    ) {
        MovimientoInventario entity = new MovimientoInventario();
        entity.setCodigoMovimiento(codigoMovimiento);
        entity.setCodigoGenerado(Boolean.TRUE);
        entity.setSku(sku);
        entity.setAlmacen(almacen);
        entity.setCompraDetalle(compraDetalle);
        entity.setReservaStock(reservaStock);
        entity.setTipoMovimiento(tipoMovimiento);
        entity.setMotivoMovimiento(motivoMovimiento);
        entity.setCantidad(defaultInteger(cantidad));
        entity.setCostoUnitario(costoUnitario);
        entity.setCostoTotal(costoTotal);
        entity.setStockAnterior(defaultInteger(stockAnterior));
        entity.setStockNuevo(defaultInteger(stockNuevo));
        entity.setReferenciaTipo(referenciaTipo);
        entity.setReferenciaIdExterno(referenciaIdExterno);
        entity.setObservacion(observacion);
        entity.setActorIdUsuarioMs1(actorIdUsuarioMs1);
        entity.setActorIdEmpleadoMs2(actorIdEmpleadoMs2);
        entity.setActorRol(actorRol);
        entity.setRequestId(requestId);
        entity.setCorrelationId(correlationId);
        entity.setEstadoMovimiento(EstadoMovimientoInventario.REGISTRADO);

        return entity;
    }

    public MovimientoInventario toEntradaEntity(
            EntradaInventarioRequestDto request,
            String codigoMovimiento,
            ProductoSku sku,
            Almacen almacen,
            Integer stockAnterior,
            Integer stockNuevo,
            Long actorIdUsuarioMs1,
            Long actorIdEmpleadoMs2,
            RolSistema actorRol,
            String requestId,
            String correlationId
    ) {
        if (request == null) {
            return null;
        }

        BigDecimal costoTotal = multiply(request.costoUnitario(), request.cantidad());

        return toEntity(
                codigoMovimiento,
                sku,
                almacen,
                null,
                null,
                request.tipoMovimiento(),
                request.motivoMovimiento(),
                request.cantidad(),
                request.costoUnitario(),
                costoTotal,
                stockAnterior,
                stockNuevo,
                request.referenciaTipo(),
                request.referenciaIdExterno(),
                request.observacion(),
                actorIdUsuarioMs1,
                actorIdEmpleadoMs2,
                actorRol,
                requestId,
                correlationId
        );
    }

    public MovimientoInventario toSalidaEntity(
            SalidaInventarioRequestDto request,
            String codigoMovimiento,
            ProductoSku sku,
            Almacen almacen,
            Integer stockAnterior,
            Integer stockNuevo,
            Long actorIdUsuarioMs1,
            Long actorIdEmpleadoMs2,
            RolSistema actorRol,
            String requestId,
            String correlationId
    ) {
        if (request == null) {
            return null;
        }

        return toEntity(
                codigoMovimiento,
                sku,
                almacen,
                null,
                null,
                request.tipoMovimiento(),
                request.motivoMovimiento(),
                request.cantidad(),
                null,
                null,
                stockAnterior,
                stockNuevo,
                request.referenciaTipo(),
                request.referenciaIdExterno(),
                request.observacion(),
                actorIdUsuarioMs1,
                actorIdEmpleadoMs2,
                actorRol,
                requestId,
                correlationId
        );
    }

    public MovimientoInventario toAjusteEntity(
            AjusteInventarioRequestDto request,
            String codigoMovimiento,
            ProductoSku sku,
            Almacen almacen,
            Integer stockAnterior,
            Integer stockNuevo,
            Long actorIdUsuarioMs1,
            Long actorIdEmpleadoMs2,
            RolSistema actorRol,
            String requestId,
            String correlationId
    ) {
        if (request == null) {
            return null;
        }

        BigDecimal costoTotal = request.tipoMovimiento() != null && request.tipoMovimiento().isEntradaFisica()
                ? multiply(request.costoUnitario(), request.cantidad())
                : null;

        return toEntity(
                codigoMovimiento,
                sku,
                almacen,
                null,
                null,
                request.tipoMovimiento(),
                request.motivoMovimiento(),
                request.cantidad(),
                request.tipoMovimiento() != null && request.tipoMovimiento().isEntradaFisica()
                        ? request.costoUnitario()
                        : null,
                costoTotal,
                stockAnterior,
                stockNuevo,
                "AJUSTE_MS3",
                request.referenciaIdExterno(),
                request.observacion(),
                actorIdUsuarioMs1,
                actorIdEmpleadoMs2,
                actorRol,
                requestId,
                correlationId
        );
    }

    public MovimientoInventarioResponseDto toResponse(MovimientoInventario entity) {
        return toResponse(entity, Moneda.PEN, true);
    }

    public MovimientoInventarioResponseDto toResponse(
            MovimientoInventario entity,
            Moneda moneda
    ) {
        return toResponse(entity, moneda, true);
    }

    public MovimientoInventarioResponseDto toResponse(
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

        return MovimientoInventarioResponseDto.builder()
                .idMovimiento(entity.getIdMovimiento())
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
                .tipoMovimiento(entity.getTipoMovimiento())
                .motivoMovimiento(entity.getMotivoMovimiento())
                .cantidad(entity.getCantidad())
                .costoUnitario(includeCosts ? toMoney(entity.getCostoUnitario(), moneda) : null)
                .costoTotal(includeCosts ? toMoney(entity.getCostoTotal(), moneda) : null)
                .stockAnterior(entity.getStockAnterior())
                .stockNuevo(entity.getStockNuevo())
                .variacionStock(variacionStock(entity))
                .referenciaTipo(entity.getReferenciaTipo())
                .referenciaIdExterno(entity.getReferenciaIdExterno())
                .observacion(entity.getObservacion())
                .actorIdUsuarioMs1(entity.getActorIdUsuarioMs1())
                .actorIdEmpleadoMs2(entity.getActorIdEmpleadoMs2())
                .actorRol(entity.getActorRol())
                .requestId(entity.getRequestId())
                .correlationId(entity.getCorrelationId())
                .estadoMovimiento(entity.getEstadoMovimiento())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public KardexResponseDto toKardexResponse(MovimientoInventario entity) {
        return toKardexResponse(entity, Moneda.PEN, true);
    }

    public KardexResponseDto toKardexResponse(
            MovimientoInventario entity,
            Moneda moneda
    ) {
        return toKardexResponse(entity, moneda, true);
    }

    public KardexResponseDto toKardexResponse(
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
        TipoMovimientoInventario tipo = entity.getTipoMovimiento();

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
                .tipoMovimiento(tipo)
                .motivoMovimiento(entity.getMotivoMovimiento())
                .entrada(tipo != null && tipo.isEntradaFisica() ? entity.getCantidad() : 0)
                .salida(tipo != null && tipo.isSalidaFisica() ? entity.getCantidad() : 0)
                .cantidad(entity.getCantidad())
                .stockAnterior(entity.getStockAnterior())
                .stockNuevo(entity.getStockNuevo())
                .variacionStock(variacionStock(entity))
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

    private Integer variacionStock(MovimientoInventario entity) {
        if (entity == null || entity.getStockAnterior() == null || entity.getStockNuevo() == null) {
            return null;
        }

        return entity.getStockNuevo() - entity.getStockAnterior();
    }

    private BigDecimal multiply(BigDecimal amount, Integer quantity) {
        if (amount == null || quantity == null) {
            return null;
        }

        return amount.multiply(BigDecimal.valueOf(quantity));
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