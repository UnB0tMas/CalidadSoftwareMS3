// ruta: src/main/java/com/upsjb/ms3/specification/KardexSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.MovimientoInventario;
import com.upsjb.ms3.domain.enums.EstadoMovimientoInventario;
import com.upsjb.ms3.dto.inventario.movimiento.filter.KardexFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;

public final class KardexSpecifications {

    private KardexSpecifications() {
    }

    public static Specification<MovimientoInventario> fromFilter(KardexFilterDto filter) {
        if (filter == null) {
            return kardexActivo();
        }

        return SpecificationBuilder.<MovimientoInventario>create()
                .textSearch(
                        filter.search(),
                        "codigoMovimiento",
                        "sku.codigoSku",
                        "sku.producto.codigoProducto",
                        "sku.producto.nombre",
                        "almacen.codigo",
                        "almacen.nombre",
                        "referenciaTipo",
                        "referenciaIdExterno",
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
                .equal("tipoMovimiento", filter.tipoMovimiento())
                .equal("motivoMovimiento", filter.motivoMovimiento())
                .equal("estadoMovimiento", filter.estadoMovimiento())
                .like("referenciaTipo", filter.referenciaTipo())
                .like("referenciaIdExterno", filter.referenciaIdExterno())
                .equal("actorIdUsuarioMs1", filter.actorIdUsuarioMs1())
                .equal("actorIdEmpleadoMs2", filter.actorIdEmpleadoMs2())
                .like("requestId", filter.requestId())
                .like("correlationId", filter.correlationId())
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", SpecificationFilterSupport.dateRange(filter.fechaMovimiento()))
                .build();
    }

    public static Specification<MovimientoInventario> kardexActivo() {
        return SpecificationBuilder.<MovimientoInventario>create()
                .activeOnly()
                .build();
    }

    public static Specification<MovimientoInventario> movimientosVigentesOnly() {
        return SpecificationBuilder.<MovimientoInventario>create()
                .activeOnly()
                .equal("estadoMovimiento", EstadoMovimientoInventario.REGISTRADO)
                .build();
    }

    public static Specification<MovimientoInventario> bySkuAndAlmacen(Long idSku, Long idAlmacen) {
        return SpecificationBuilder.<MovimientoInventario>create()
                .activeOnly()
                .equal("sku.idSku", idSku)
                .equal("almacen.idAlmacen", idAlmacen)
                .build();
    }
}