// ruta: src/main/java/com/upsjb/ms3/mapper/CompraInventarioDetalleMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.CompraInventario;
import com.upsjb.ms3.domain.entity.CompraInventarioDetalle;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioDetalleRequestDto;
import com.upsjb.ms3.dto.inventario.compra.response.CompraInventarioDetalleResponseDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class CompraInventarioDetalleMapper {

    public CompraInventarioDetalle toEntity(
            CompraInventarioDetalleRequestDto request,
            CompraInventario compra,
            ProductoSku sku,
            Almacen almacen,
            BigDecimal costoTotal
    ) {
        if (request == null) {
            return null;
        }

        CompraInventarioDetalle entity = new CompraInventarioDetalle();
        entity.setCompra(compra);
        entity.setSku(sku);
        entity.setAlmacen(almacen);
        entity.setCantidad(request.cantidad());
        entity.setCostoUnitario(request.costoUnitario());
        entity.setDescuento(defaultAmount(request.descuento()));
        entity.setImpuesto(defaultAmount(request.impuesto()));
        entity.setCostoTotal(defaultAmount(costoTotal));

        return entity;
    }

    public void updateEntity(
            CompraInventarioDetalle entity,
            CompraInventarioDetalleRequestDto request,
            ProductoSku sku,
            Almacen almacen,
            BigDecimal costoTotal
    ) {
        if (entity == null || request == null) {
            return;
        }

        entity.setSku(sku);
        entity.setAlmacen(almacen);
        entity.setCantidad(request.cantidad());
        entity.setCostoUnitario(request.costoUnitario());
        entity.setDescuento(defaultAmount(request.descuento()));
        entity.setImpuesto(defaultAmount(request.impuesto()));
        entity.setCostoTotal(defaultAmount(costoTotal));
    }

    public CompraInventarioDetalleResponseDto toResponse(CompraInventarioDetalle entity) {
        if (entity == null) {
            return null;
        }

        CompraInventario compra = entity.getCompra();
        Moneda moneda = compra == null ? Moneda.PEN : compra.getMoneda();
        ProductoSku sku = entity.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        Almacen almacen = entity.getAlmacen();

        return CompraInventarioDetalleResponseDto.builder()
                .idCompraDetalle(entity.getIdCompraDetalle())
                .idCompra(compra == null ? null : compra.getIdCompra())
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