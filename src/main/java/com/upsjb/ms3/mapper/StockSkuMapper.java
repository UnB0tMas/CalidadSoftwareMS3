// ruta: src/main/java/com/upsjb/ms3/mapper/StockSkuMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.dto.inventario.stock.response.StockDisponibleResponseDto;
import com.upsjb.ms3.dto.inventario.stock.response.StockSkuDetailResponseDto;
import com.upsjb.ms3.dto.inventario.stock.response.StockSkuResponseDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import com.upsjb.ms3.dto.shared.StockResumenResponseDto;
import com.upsjb.ms3.util.StockMathUtil;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class StockSkuMapper {

    public StockSkuResponseDto toResponse(StockSku entity) {
        return toResponse(entity, Moneda.PEN, true);
    }

    public StockSkuResponseDto toResponse(StockSku entity, Moneda moneda) {
        return toResponse(entity, moneda, true);
    }

    public StockSkuResponseDto toResponse(StockSku entity, Moneda moneda, boolean includeCosts) {
        if (entity == null) {
            return null;
        }

        ProductoSku sku = entity.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        Almacen almacen = entity.getAlmacen();

        return StockSkuResponseDto.builder()
                .idStock(entity.getIdStock())
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .barcode(sku == null ? null : sku.getBarcode())
                .estadoSku(sku == null ? null : sku.getEstadoSku())
                .color(sku == null ? null : sku.getColor())
                .talla(sku == null ? null : sku.getTalla())
                .modelo(sku == null ? null : sku.getModelo())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .idAlmacen(almacen == null ? null : almacen.getIdAlmacen())
                .codigoAlmacen(almacen == null ? null : almacen.getCodigo())
                .nombreAlmacen(almacen == null ? null : almacen.getNombre())
                .almacenPrincipal(almacen == null ? null : almacen.getPrincipal())
                .stockFisico(defaultInteger(entity.getStockFisico()))
                .stockReservado(defaultInteger(entity.getStockReservado()))
                .stockDisponible(resolveStockDisponible(entity))
                .stockMinimo(defaultInteger(entity.getStockMinimo()))
                .stockMaximo(entity.getStockMaximo())
                .bajoStock(isBajoStock(entity))
                .sobreStock(isSobreStock(entity))
                .costoPromedioActual(includeCosts ? toMoney(entity.getCostoPromedioActual(), moneda) : null)
                .ultimoCostoCompra(includeCosts ? toMoney(entity.getUltimoCostoCompra(), moneda) : null)
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public StockSkuDetailResponseDto toDetailResponse(StockSku entity) {
        return toDetailResponse(entity, Moneda.PEN, true);
    }

    public StockSkuDetailResponseDto toDetailResponse(StockSku entity, Moneda moneda) {
        return toDetailResponse(entity, moneda, true);
    }

    public StockSkuDetailResponseDto toDetailResponse(StockSku entity, Moneda moneda, boolean includeCosts) {
        if (entity == null) {
            return null;
        }

        ProductoSku sku = entity.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        Almacen almacen = entity.getAlmacen();

        return StockSkuDetailResponseDto.builder()
                .idStock(entity.getIdStock())
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .barcode(sku == null ? null : sku.getBarcode())
                .estadoSku(sku == null ? null : sku.getEstadoSku())
                .color(sku == null ? null : sku.getColor())
                .talla(sku == null ? null : sku.getTalla())
                .material(sku == null ? null : sku.getMaterial())
                .modelo(sku == null ? null : sku.getModelo())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .idAlmacen(almacen == null ? null : almacen.getIdAlmacen())
                .codigoAlmacen(almacen == null ? null : almacen.getCodigo())
                .nombreAlmacen(almacen == null ? null : almacen.getNombre())
                .almacenPrincipal(almacen == null ? null : almacen.getPrincipal())
                .almacenPermiteVenta(almacen == null ? null : almacen.getPermiteVenta())
                .almacenPermiteCompra(almacen == null ? null : almacen.getPermiteCompra())
                .stockFisico(defaultInteger(entity.getStockFisico()))
                .stockReservado(defaultInteger(entity.getStockReservado()))
                .stockDisponible(resolveStockDisponible(entity))
                .stockMinimo(defaultInteger(entity.getStockMinimo()))
                .stockMaximo(entity.getStockMaximo())
                .bajoStock(isBajoStock(entity))
                .sobreStock(isSobreStock(entity))
                .costoPromedioActual(includeCosts ? toMoney(entity.getCostoPromedioActual(), moneda) : null)
                .ultimoCostoCompra(includeCosts ? toMoney(entity.getUltimoCostoCompra(), moneda) : null)
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public StockDisponibleResponseDto toDisponibleResponse(
            StockSku entity,
            Integer cantidadSolicitada
    ) {
        if (entity == null) {
            return null;
        }

        ProductoSku sku = entity.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        Almacen almacen = entity.getAlmacen();
        Integer disponible = resolveStockDisponible(entity);

        return StockDisponibleResponseDto.builder()
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .barcode(sku == null ? null : sku.getBarcode())
                .estadoSku(sku == null ? null : sku.getEstadoSku())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .idAlmacen(almacen == null ? null : almacen.getIdAlmacen())
                .codigoAlmacen(almacen == null ? null : almacen.getCodigo())
                .nombreAlmacen(almacen == null ? null : almacen.getNombre())
                .stockFisico(defaultInteger(entity.getStockFisico()))
                .stockReservado(defaultInteger(entity.getStockReservado()))
                .stockDisponible(disponible)
                .disponible(disponible > 0)
                .cantidadSolicitada(cantidadSolicitada)
                .cantidadDisponible(resolveCantidadDisponible(disponible, cantidadSolicitada))
                .build();
    }

    public StockDisponibleResponseDto toDisponibleResponse(
            ProductoSku sku,
            Almacen almacen,
            Integer cantidadSolicitada
    ) {
        Producto producto = sku == null ? null : sku.getProducto();

        return StockDisponibleResponseDto.builder()
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .barcode(sku == null ? null : sku.getBarcode())
                .estadoSku(sku == null ? null : sku.getEstadoSku())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .idAlmacen(almacen == null ? null : almacen.getIdAlmacen())
                .codigoAlmacen(almacen == null ? null : almacen.getCodigo())
                .nombreAlmacen(almacen == null ? null : almacen.getNombre())
                .stockFisico(0)
                .stockReservado(0)
                .stockDisponible(0)
                .disponible(Boolean.FALSE)
                .cantidadSolicitada(cantidadSolicitada)
                .cantidadDisponible(resolveCantidadDisponible(0, cantidadSolicitada))
                .build();
    }

    public StockResumenResponseDto toResumenResponse(StockSku entity) {
        if (entity == null) {
            return null;
        }

        ProductoSku sku = entity.getSku();
        Almacen almacen = entity.getAlmacen();

        return StockResumenResponseDto.builder()
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .idAlmacen(almacen == null ? null : almacen.getIdAlmacen())
                .codigoAlmacen(almacen == null ? null : almacen.getCodigo())
                .nombreAlmacen(almacen == null ? null : almacen.getNombre())
                .stockFisico(defaultInteger(entity.getStockFisico()))
                .stockReservado(defaultInteger(entity.getStockReservado()))
                .stockDisponible(resolveStockDisponible(entity))
                .stockMinimo(defaultInteger(entity.getStockMinimo()))
                .stockMaximo(entity.getStockMaximo())
                .bajoStock(isBajoStock(entity))
                .build();
    }

    private Integer resolveStockDisponible(StockSku entity) {
        if (entity == null) {
            return 0;
        }

        if (entity.getStockDisponible() != null) {
            return entity.getStockDisponible();
        }

        return StockMathUtil.available(entity.getStockFisico(), entity.getStockReservado());
    }

    private Boolean resolveCantidadDisponible(Integer disponible, Integer cantidadSolicitada) {
        if (cantidadSolicitada == null) {
            return null;
        }

        return defaultInteger(disponible) >= cantidadSolicitada;
    }

    private Boolean isBajoStock(StockSku entity) {
        if (entity == null) {
            return false;
        }

        return StockMathUtil.isLowStock(resolveStockDisponible(entity), entity.getStockMinimo());
    }

    private Boolean isSobreStock(StockSku entity) {
        if (entity == null || entity.getStockMaximo() == null) {
            return false;
        }

        return defaultInteger(entity.getStockFisico()) > entity.getStockMaximo();
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