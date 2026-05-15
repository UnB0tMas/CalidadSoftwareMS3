// ruta: src/main/java/com/upsjb/ms3/specification/MovimientoInventarioSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.MovimientoInventario;
import com.upsjb.ms3.dto.inventario.movimiento.filter.MovimientoInventarioFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;

public final class MovimientoInventarioSpecifications {

    private MovimientoInventarioSpecifications() {
    }

    public static Specification<MovimientoInventario> fromFilter(MovimientoInventarioFilterDto filter) {
        if (filter == null) {
            return activeOnly();
        }

        return SpecificationBuilder.<MovimientoInventario>create()
                .textSearch(
                        filter.search(),
                        "codigoMovimiento",
                        "sku.codigoSku",
                        "sku.barcode",
                        "sku.producto.codigoProducto",
                        "sku.producto.nombre",
                        "almacen.codigo",
                        "almacen.nombre",
                        "referenciaTipo",
                        "referenciaIdExterno",
                        "observacion",
                        "requestId",
                        "correlationId"
                )
                .equal("idMovimiento", filter.idMovimiento())
                .like("codigoMovimiento", filter.codigoMovimiento())
                .equal("sku.idSku", filter.idSku())
                .like("sku.codigoSku", filter.codigoSku())
                .equal("sku.producto.idProducto", filter.idProducto())
                .like("sku.producto.codigoProducto", filter.codigoProducto())
                .equal("almacen.idAlmacen", filter.idAlmacen())
                .like("almacen.codigo", filter.codigoAlmacen())
                .equal("compraDetalle.idCompraDetalle", filter.idCompraDetalle())
                .equal("reservaStock.idReservaStock", filter.idReservaStock())
                .equal("tipoMovimiento", filter.tipoMovimiento())
                .equal("motivoMovimiento", filter.motivoMovimiento())
                .equal("estadoMovimiento", filter.estadoMovimiento())
                .like("referenciaTipo", filter.referenciaTipo())
                .like("referenciaIdExterno", filter.referenciaIdExterno())
                .equal("actorIdUsuarioMs1", filter.actorIdUsuarioMs1())
                .equal("actorIdEmpleadoMs2", filter.actorIdEmpleadoMs2())
                .equal("actorRol", filter.actorRol())
                .like("requestId", filter.requestId())
                .like("correlationId", filter.correlationId())
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", SpecificationFilterSupport.dateRange(filter.fechaMovimiento()))
                .build();
    }

    public static Specification<MovimientoInventario> activeOnly() {
        return SpecificationBuilder.<MovimientoInventario>create()
                .activeOnly()
                .build();
    }

    public static Specification<MovimientoInventario> bySku(Long idSku) {
        return SpecificationBuilder.<MovimientoInventario>create()
                .activeOnly()
                .equal("sku.idSku", idSku)
                .build();
    }

    public static Specification<MovimientoInventario> byAlmacen(Long idAlmacen) {
        return SpecificationBuilder.<MovimientoInventario>create()
                .activeOnly()
                .equal("almacen.idAlmacen", idAlmacen)
                .build();
    }

    public static Specification<MovimientoInventario> byReferencia(String referenciaTipo, String referenciaIdExterno) {
        return SpecificationBuilder.<MovimientoInventario>create()
                .activeOnly()
                .like("referenciaTipo", referenciaTipo)
                .like("referenciaIdExterno", referenciaIdExterno)
                .build();
    }
}