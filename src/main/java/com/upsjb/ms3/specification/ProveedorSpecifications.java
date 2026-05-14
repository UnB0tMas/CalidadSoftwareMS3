// ruta: src/main/java/com/upsjb/ms3/specification/ProveedorSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.Proveedor;
import com.upsjb.ms3.dto.proveedor.filter.ProveedorFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;

public final class ProveedorSpecifications {

    private ProveedorSpecifications() {
    }

    public static Specification<Proveedor> fromFilter(ProveedorFilterDto filter) {
        if (filter == null) {
            return activeOnly();
        }

        return SpecificationBuilder.<Proveedor>create()
                .textSearch(
                        filter.search(),
                        "ruc",
                        "numeroDocumento",
                        "razonSocial",
                        "nombreComercial",
                        "nombres",
                        "apellidos",
                        "correo",
                        "telefono"
                )
                .equal("tipoProveedor", filter.tipoProveedor())
                .equal("tipoDocumento", filter.tipoDocumento())
                .like("numeroDocumento", filter.numeroDocumento())
                .like("ruc", filter.ruc())
                .like("razonSocial", filter.razonSocial())
                .like("nombreComercial", filter.nombreComercial())
                .like("nombres", filter.nombres())
                .like("apellidos", filter.apellidos())
                .like("correo", filter.correo())
                .like("telefono", filter.telefono())
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", SpecificationFilterSupport.dateRange(filter.fechaCreacion()))
                .build();
    }

    public static Specification<Proveedor> activeOnly() {
        return SpecificationBuilder.<Proveedor>create()
                .activeOnly()
                .build();
    }

    public static Specification<Proveedor> byDocumento(String documento) {
        return SpecificationBuilder.<Proveedor>create()
                .textSearch(documento, "ruc", "numeroDocumento")
                .build();
    }

    public static Specification<Proveedor> byNombreComercial(String nombre) {
        return SpecificationBuilder.<Proveedor>create()
                .textSearch(
                        nombre,
                        "razonSocial",
                        "nombreComercial",
                        "nombres",
                        "apellidos"
                )
                .build();
    }

    public static Specification<Proveedor> byTipoProveedorActivo(
            com.upsjb.ms3.domain.enums.TipoProveedor tipoProveedor
    ) {
        return SpecificationBuilder.<Proveedor>create()
                .activeOnly()
                .equal("tipoProveedor", tipoProveedor)
                .build();
    }
}