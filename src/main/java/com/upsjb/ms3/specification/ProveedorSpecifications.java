// ruta: src/main/java/com/upsjb/ms3/specification/ProveedorSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.Proveedor;
import com.upsjb.ms3.domain.enums.TipoProveedor;
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
                        "telefono",
                        "direccion"
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
                .like("direccion", filter.direccion())
                .equal("creadoPorIdUsuarioMs1", filter.creadoPorIdUsuarioMs1())
                .equal("actualizadoPorIdUsuarioMs1", filter.actualizadoPorIdUsuarioMs1())
                .bool("estado", BooleanCriteria.of(resolveEstado(filter)))
                .range("createdAt", SpecificationFilterSupport.dateRange(filter.fechaCreacion()))
                .range("updatedAt", SpecificationFilterSupport.dateRange(filter.fechaActualizacion()))
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

    public static Specification<Proveedor> byTipoProveedorActivo(TipoProveedor tipoProveedor) {
        return SpecificationBuilder.<Proveedor>create()
                .activeOnly()
                .equal("tipoProveedor", tipoProveedor)
                .build();
    }

    private static Boolean resolveEstado(ProveedorFilterDto filter) {
        if (filter == null) {
            return Boolean.TRUE;
        }

        if (Boolean.TRUE.equals(filter.incluirTodosLosEstados())) {
            return null;
        }

        return filter.estado() == null ? Boolean.TRUE : filter.estado();
    }
}