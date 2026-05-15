// ruta: src/main/java/com/upsjb/ms3/specification/StockSkuSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.dto.inventario.stock.filter.StockSkuFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.NumericRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;

public final class StockSkuSpecifications {

    private StockSkuSpecifications() {
    }

    public static Specification<StockSku> fromFilter(StockSkuFilterDto filter) {
        if (filter == null) {
            return activeOnly();
        }

        return SpecificationBuilder.<StockSku>create()
                .textSearch(
                        filter.search(),
                        "sku.codigoSku",
                        "sku.barcode",
                        "sku.color",
                        "sku.talla",
                        "sku.modelo",
                        "sku.material",
                        "sku.producto.codigoProducto",
                        "sku.producto.nombre",
                        "almacen.codigo",
                        "almacen.nombre"
                )
                .equal("sku.idSku", filter.idSku())
                .like("sku.codigoSku", filter.codigoSku())
                .like("sku.barcode", filter.barcode())
                .equal("sku.estadoSku", filter.estadoSku())
                .equal("sku.producto.idProducto", filter.idProducto())
                .like("sku.producto.codigoProducto", filter.codigoProducto())
                .like("sku.producto.nombre", filter.nombreProducto())
                .equal("almacen.idAlmacen", filter.idAlmacen())
                .like("almacen.codigo", filter.codigoAlmacen())
                .like("almacen.nombre", filter.nombreAlmacen())
                .numericRange(
                        "stockFisico",
                        new NumericRangeCriteria<>(filter.stockFisicoMin(), filter.stockFisicoMax())
                )
                .numericRange(
                        "stockReservado",
                        new NumericRangeCriteria<>(filter.stockReservadoMin(), filter.stockReservadoMax())
                )
                .numericRange(
                        "stockDisponible",
                        new NumericRangeCriteria<>(filter.stockDisponibleMin(), filter.stockDisponibleMax())
                )
                .numericRange(
                        "stockMinimo",
                        new NumericRangeCriteria<>(filter.stockMinimoMin(), filter.stockMinimoMax())
                )
                .numericRange(
                        "stockMaximo",
                        new NumericRangeCriteria<>(filter.stockMaximoMin(), filter.stockMaximoMax())
                )
                .bool("estado", BooleanCriteria.of(resolveEstado(filter)))
                .range("createdAt", SpecificationFilterSupport.dateRange(filter.fechaCreacion()))
                .range("updatedAt", SpecificationFilterSupport.dateRange(filter.fechaActualizacion()))
                .and(bajoStock(filter.bajoStock()))
                .and(sobreStock(filter.sobreStock()))
                .and(conStockDisponible(filter.conStockDisponible()))
                .build();
    }

    public static Specification<StockSku> activeOnly() {
        return SpecificationBuilder.<StockSku>create()
                .activeOnly()
                .build();
    }

    public static Specification<StockSku> bySku(Long idSku) {
        return SpecificationBuilder.<StockSku>create()
                .equal("sku.idSku", idSku)
                .build();
    }

    public static Specification<StockSku> byProducto(Long idProducto) {
        return SpecificationBuilder.<StockSku>create()
                .equal("sku.producto.idProducto", idProducto)
                .build();
    }

    public static Specification<StockSku> byAlmacen(Long idAlmacen) {
        return SpecificationBuilder.<StockSku>create()
                .equal("almacen.idAlmacen", idAlmacen)
                .build();
    }

    public static Specification<StockSku> bajoStock(Boolean bajoStock) {
        return (root, query, cb) -> {
            if (bajoStock == null) {
                return cb.conjunction();
            }

            if (Boolean.TRUE.equals(bajoStock)) {
                return cb.lessThanOrEqualTo(root.get("stockDisponible"), root.get("stockMinimo"));
            }

            return cb.greaterThan(root.get("stockDisponible"), root.get("stockMinimo"));
        };
    }

    public static Specification<StockSku> sobreStock(Boolean sobreStock) {
        return (root, query, cb) -> {
            if (sobreStock == null) {
                return cb.conjunction();
            }

            if (Boolean.TRUE.equals(sobreStock)) {
                return cb.and(
                        cb.isNotNull(root.get("stockMaximo")),
                        cb.greaterThan(root.get("stockFisico"), root.get("stockMaximo"))
                );
            }

            return cb.or(
                    cb.isNull(root.get("stockMaximo")),
                    cb.lessThanOrEqualTo(root.get("stockFisico"), root.get("stockMaximo"))
            );
        };
    }

    public static Specification<StockSku> conStockDisponible(Boolean conStockDisponible) {
        return (root, query, cb) -> {
            if (conStockDisponible == null) {
                return cb.conjunction();
            }

            if (Boolean.TRUE.equals(conStockDisponible)) {
                return cb.greaterThan(root.get("stockDisponible"), 0);
            }

            return cb.lessThanOrEqualTo(root.get("stockDisponible"), 0);
        };
    }

    private static Boolean resolveEstado(StockSkuFilterDto filter) {
        if (filter == null) {
            return Boolean.TRUE;
        }

        if (Boolean.TRUE.equals(filter.incluirTodosLosEstados())) {
            return null;
        }

        return filter.estado() == null ? Boolean.TRUE : filter.estado();
    }
}