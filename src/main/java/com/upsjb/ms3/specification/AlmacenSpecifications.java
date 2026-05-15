// ruta: src/main/java/com/upsjb/ms3/specification/AlmacenSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.dto.inventario.almacen.filter.AlmacenFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;

public final class AlmacenSpecifications {

    private AlmacenSpecifications() {
    }

    public static Specification<Almacen> fromFilter(AlmacenFilterDto filter) {
        if (filter == null) {
            return activeOnly();
        }

        return SpecificationBuilder.<Almacen>create()
                .textSearch(
                        filter.search(),
                        "codigo",
                        "nombre",
                        "direccion",
                        "observacion"
                )
                .like("codigo", filter.codigo())
                .like("nombre", filter.nombre())
                .like("direccion", filter.direccion())
                .bool("principal", BooleanCriteria.of(filter.principal()))
                .bool("permiteVenta", BooleanCriteria.of(filter.permiteVenta()))
                .bool("permiteCompra", BooleanCriteria.of(filter.permiteCompra()))
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", SpecificationFilterSupport.dateRange(filter.fechaCreacion()))
                .build();
    }

    public static Specification<Almacen> activeOnly() {
        return SpecificationBuilder.<Almacen>create()
                .activeOnly()
                .build();
    }

    public static Specification<Almacen> principalesOnly() {
        return SpecificationBuilder.<Almacen>create()
                .activeOnly()
                .equal("principal", Boolean.TRUE)
                .build();
    }

    public static Specification<Almacen> ventaEnabled() {
        return SpecificationBuilder.<Almacen>create()
                .activeOnly()
                .equal("permiteVenta", Boolean.TRUE)
                .build();
    }

    public static Specification<Almacen> compraEnabled() {
        return SpecificationBuilder.<Almacen>create()
                .activeOnly()
                .equal("permiteCompra", Boolean.TRUE)
                .build();
    }

    public static Specification<Almacen> byCodigo(String codigo) {
        return SpecificationBuilder.<Almacen>create()
                .activeOnly()
                .like("codigo", codigo)
                .build();
    }

    public static Specification<Almacen> byNombre(String nombre) {
        return SpecificationBuilder.<Almacen>create()
                .activeOnly()
                .like("nombre", nombre)
                .build();
    }

    public static Specification<Almacen> byPrincipal(Boolean principal) {
        return SpecificationBuilder.<Almacen>create()
                .activeOnly()
                .bool("principal", BooleanCriteria.of(principal))
                .build();
    }

    public static Specification<Almacen> byPermiteVenta(Boolean permiteVenta) {
        return SpecificationBuilder.<Almacen>create()
                .activeOnly()
                .bool("permiteVenta", BooleanCriteria.of(permiteVenta))
                .build();
    }

    public static Specification<Almacen> byPermiteCompra(Boolean permiteCompra) {
        return SpecificationBuilder.<Almacen>create()
                .activeOnly()
                .bool("permiteCompra", BooleanCriteria.of(permiteCompra))
                .build();
    }
}