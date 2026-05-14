// ruta: src/main/java/com/upsjb/ms3/mapper/CompraInventarioMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.CompraInventario;
import com.upsjb.ms3.domain.entity.CompraInventarioDetalle;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.Proveedor;
import com.upsjb.ms3.domain.enums.EstadoCompraInventario;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioCreateRequestDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioDetalleRequestDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioUpdateRequestDto;
import com.upsjb.ms3.dto.inventario.compra.response.CompraInventarioDetailResponseDto;
import com.upsjb.ms3.dto.inventario.compra.response.CompraInventarioDetalleResponseDto;
import com.upsjb.ms3.dto.inventario.compra.response.CompraInventarioResponseDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompraInventarioMapper {

    private final ProveedorMapper proveedorMapper;

    public CompraInventario toEntity(
            CompraInventarioCreateRequestDto request,
            Proveedor proveedor,
            String codigoCompra,
            Long creadoPorIdUsuarioMs1
    ) {
        if (request == null) {
            return null;
        }

        CompraInventario entity = new CompraInventario();
        entity.setCodigoCompra(codigoCompra);
        entity.setCodigoGenerado(Boolean.TRUE);
        entity.setProveedor(proveedor);
        entity.setFechaCompra(request.fechaCompra() == null ? LocalDateTime.now() : request.fechaCompra());
        entity.setMoneda(request.moneda() == null ? Moneda.PEN : request.moneda());
        entity.setSubtotal(BigDecimal.ZERO);
        entity.setDescuentoTotal(BigDecimal.ZERO);
        entity.setImpuestoTotal(BigDecimal.ZERO);
        entity.setTotal(BigDecimal.ZERO);
        entity.setEstadoCompra(EstadoCompraInventario.BORRADOR);
        entity.setObservacion(request.observacion());
        entity.setCreadoPorIdUsuarioMs1(creadoPorIdUsuarioMs1);
        return entity;
    }

    public void updateEntity(
            CompraInventario entity,
            CompraInventarioUpdateRequestDto request,
            Proveedor proveedor
    ) {
        if (entity == null || request == null) {
            return;
        }

        entity.setProveedor(proveedor);
        entity.setFechaCompra(request.fechaCompra() == null ? entity.getFechaCompra() : request.fechaCompra());
        entity.setMoneda(request.moneda() == null ? entity.getMoneda() : request.moneda());
        entity.setObservacion(request.observacion());
    }

    public CompraInventarioDetalle toDetalleEntity(
            CompraInventario compra,
            CompraInventarioDetalleRequestDto request,
            ProductoSku sku,
            Almacen almacen
    ) {
        if (compra == null || request == null) {
            return null;
        }

        BigDecimal descuento = defaultAmount(request.descuento());
        BigDecimal impuesto = defaultAmount(request.impuesto());
        BigDecimal subtotal = request.costoUnitario().multiply(BigDecimal.valueOf(request.cantidad()));
        BigDecimal costoTotal = subtotal.subtract(descuento).add(impuesto);

        CompraInventarioDetalle detalle = new CompraInventarioDetalle();
        detalle.setCompra(compra);
        detalle.setSku(sku);
        detalle.setAlmacen(almacen);
        detalle.setCantidad(request.cantidad());
        detalle.setCostoUnitario(request.costoUnitario());
        detalle.setDescuento(descuento);
        detalle.setImpuesto(impuesto);
        detalle.setCostoTotal(costoTotal);
        detalle.activar();

        return detalle;
    }

    public CompraInventarioDetalleResponseDto toDetalleResponse(
            CompraInventarioDetalle entity,
            Moneda moneda
    ) {
        if (entity == null) {
            return null;
        }

        ProductoSku sku = entity.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        Almacen almacen = entity.getAlmacen();

        return CompraInventarioDetalleResponseDto.builder()
                .idCompraDetalle(entity.getIdCompraDetalle())
                .idCompra(entity.getCompra() == null ? null : entity.getCompra().getIdCompra())
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .idAlmacen(almacen == null ? null : almacen.getIdAlmacen())
                .codigoAlmacen(almacen == null ? null : almacen.getCodigo())
                .nombreAlmacen(almacen == null ? null : almacen.getNombre())
                .cantidad(entity.getCantidad())
                .costoUnitario(toMoney(entity.getCostoUnitario(), moneda))
                .descuento(toMoney(entity.getDescuento(), moneda))
                .impuesto(toMoney(entity.getImpuesto(), moneda))
                .costoTotal(toMoney(entity.getCostoTotal(), moneda))
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void applyTotals(
            CompraInventario entity,
            BigDecimal subtotal,
            BigDecimal descuentoTotal,
            BigDecimal impuestoTotal,
            BigDecimal total
    ) {
        if (entity == null) {
            return;
        }

        entity.setSubtotal(defaultAmount(subtotal));
        entity.setDescuentoTotal(defaultAmount(descuentoTotal));
        entity.setImpuestoTotal(defaultAmount(impuestoTotal));
        entity.setTotal(defaultAmount(total));
    }

    public void markConfirmada(
            CompraInventario entity,
            Long confirmadoPorIdUsuarioMs1,
            LocalDateTime confirmadoAt
    ) {
        if (entity == null) {
            return;
        }

        entity.setEstadoCompra(EstadoCompraInventario.CONFIRMADA);
        entity.setConfirmadoPorIdUsuarioMs1(confirmadoPorIdUsuarioMs1);
        entity.setConfirmadoAt(confirmadoAt == null ? LocalDateTime.now() : confirmadoAt);
    }

    public void markAnulada(CompraInventario entity) {
        if (entity == null) {
            return;
        }

        entity.setEstadoCompra(EstadoCompraInventario.ANULADA);
    }

    public CompraInventarioResponseDto toResponse(CompraInventario entity) {
        if (entity == null) {
            return null;
        }

        Moneda moneda = entity.getMoneda();

        return CompraInventarioResponseDto.builder()
                .idCompra(entity.getIdCompra())
                .codigoCompra(entity.getCodigoCompra())
                .codigoGenerado(entity.getCodigoGenerado())
                .idProveedor(entity.getProveedor() == null ? null : entity.getProveedor().getIdProveedor())
                .proveedorDisplay(proveedorMapper.displayName(entity.getProveedor()))
                .fechaCompra(entity.getFechaCompra())
                .moneda(moneda)
                .subtotal(toMoney(entity.getSubtotal(), moneda))
                .descuentoTotal(toMoney(entity.getDescuentoTotal(), moneda))
                .impuestoTotal(toMoney(entity.getImpuestoTotal(), moneda))
                .total(toMoney(entity.getTotal(), moneda))
                .estadoCompra(entity.getEstadoCompra())
                .observacion(entity.getObservacion())
                .creadoPorIdUsuarioMs1(entity.getCreadoPorIdUsuarioMs1())
                .confirmadoPorIdUsuarioMs1(entity.getConfirmadoPorIdUsuarioMs1())
                .confirmadoAt(entity.getConfirmadoAt())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CompraInventarioDetailResponseDto toDetailResponse(
            CompraInventario entity,
            List<CompraInventarioDetalleResponseDto> detalles
    ) {
        if (entity == null) {
            return null;
        }

        Moneda moneda = entity.getMoneda();

        return CompraInventarioDetailResponseDto.builder()
                .idCompra(entity.getIdCompra())
                .codigoCompra(entity.getCodigoCompra())
                .codigoGenerado(entity.getCodigoGenerado())
                .idProveedor(entity.getProveedor() == null ? null : entity.getProveedor().getIdProveedor())
                .proveedorDisplay(proveedorMapper.displayName(entity.getProveedor()))
                .fechaCompra(entity.getFechaCompra())
                .moneda(moneda)
                .subtotal(toMoney(entity.getSubtotal(), moneda))
                .descuentoTotal(toMoney(entity.getDescuentoTotal(), moneda))
                .impuestoTotal(toMoney(entity.getImpuestoTotal(), moneda))
                .total(toMoney(entity.getTotal(), moneda))
                .estadoCompra(entity.getEstadoCompra())
                .observacion(entity.getObservacion())
                .creadoPorIdUsuarioMs1(entity.getCreadoPorIdUsuarioMs1())
                .confirmadoPorIdUsuarioMs1(entity.getConfirmadoPorIdUsuarioMs1())
                .confirmadoAt(entity.getConfirmadoAt())
                .estado(entity.getEstado())
                .detalles(detalles == null ? List.of() : detalles)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
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

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}