// ruta: src/main/java/com/upsjb/ms3/specification/CompraInventarioSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.CompraInventario;
import com.upsjb.ms3.domain.enums.EstadoCompraInventario;
import com.upsjb.ms3.dto.inventario.compra.filter.CompraInventarioFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;

public final class CompraInventarioSpecifications {

    private CompraInventarioSpecifications() {
    }

    public static Specification<CompraInventario> fromFilter(CompraInventarioFilterDto filter) {
        if (filter == null) {
            return activeOnly();
        }

        return SpecificationBuilder.<CompraInventario>create()
                .textSearch(
                        filter.search(),
                        "codigoCompra",
                        "proveedor.ruc",
                        "proveedor.numeroDocumento",
                        "proveedor.razonSocial",
                        "proveedor.nombreComercial",
                        "proveedor.nombres",
                        "proveedor.apellidos",
                        "observacion"
                )
                .like("codigoCompra", filter.codigoCompra())
                .equal("proveedor.idProveedor", filter.idProveedor())
                .textSearch(filter.proveedorDocumento(), "proveedor.ruc", "proveedor.numeroDocumento")
                .textSearch(
                        filter.proveedorNombre(),
                        "proveedor.razonSocial",
                        "proveedor.nombreComercial",
                        "proveedor.nombres",
                        "proveedor.apellidos"
                )
                .equal("moneda", filter.moneda())
                .equal("estadoCompra", filter.estadoCompra())
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("fechaCompra", SpecificationFilterSupport.dateRange(filter.fechaCompra()))
                .range("createdAt", SpecificationFilterSupport.dateRange(filter.fechaCreacion()))
                .build();
    }

    public static Specification<CompraInventario> activeOnly() {
        return SpecificationBuilder.<CompraInventario>create()
                .activeOnly()
                .build();
    }

    public static Specification<CompraInventario> borradorOnly() {
        return byEstadoCompra(EstadoCompraInventario.BORRADOR);
    }

    public static Specification<CompraInventario> confirmadasOnly() {
        return byEstadoCompra(EstadoCompraInventario.CONFIRMADA);
    }

    public static Specification<CompraInventario> byEstadoCompra(EstadoCompraInventario estadoCompra) {
        return SpecificationBuilder.<CompraInventario>create()
                .activeOnly()
                .equal("estadoCompra", estadoCompra)
                .build();
    }

    public static Specification<CompraInventario> byProveedor(Long idProveedor) {
        return SpecificationBuilder.<CompraInventario>create()
                .equal("proveedor.idProveedor", idProveedor)
                .build();
    }
}