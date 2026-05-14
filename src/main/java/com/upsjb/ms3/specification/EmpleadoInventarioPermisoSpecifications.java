// ruta: src/main/java/com/upsjb/ms3/specification/EmpleadoInventarioPermisoSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.EmpleadoInventarioPermisoHistorial;
import com.upsjb.ms3.dto.empleado.filter.EmpleadoInventarioPermisoFilterDto;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.DateRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class EmpleadoInventarioPermisoSpecifications {

    private EmpleadoInventarioPermisoSpecifications() {
    }

    public static Specification<EmpleadoInventarioPermisoHistorial> fromFilter(
            EmpleadoInventarioPermisoFilterDto filter
    ) {
        if (filter == null) {
            return activeOnly();
        }

        return SpecificationBuilder.<EmpleadoInventarioPermisoHistorial>create()
                .textSearch(
                        filter.search(),
                        "empleadoSnapshot.codigoEmpleado",
                        "empleadoSnapshot.nombresCompletos",
                        "empleadoSnapshot.areaCodigo",
                        "empleadoSnapshot.areaNombre",
                        "motivo"
                )
                .equal("empleadoSnapshot.idEmpleadoSnapshot", filter.idEmpleadoSnapshot())
                .equal("empleadoSnapshot.idEmpleadoMs2", filter.idEmpleadoMs2())
                .equal("empleadoSnapshot.idUsuarioMs1", filter.idUsuarioMs1())
                .like("empleadoSnapshot.codigoEmpleado", filter.codigoEmpleado())
                .like("empleadoSnapshot.areaCodigo", filter.areaCodigo())
                .bool("empleadoSnapshot.empleadoActivo", BooleanCriteria.of(filter.empleadoActivo()))
                .bool("vigente", BooleanCriteria.of(filter.vigente()))
                .bool("puedeCrearProductoBasico", BooleanCriteria.of(filter.puedeCrearProductoBasico()))
                .bool("puedeEditarProductoBasico", BooleanCriteria.of(filter.puedeEditarProductoBasico()))
                .bool("puedeRegistrarEntrada", BooleanCriteria.of(filter.puedeRegistrarEntrada()))
                .bool("puedeRegistrarSalida", BooleanCriteria.of(filter.puedeRegistrarSalida()))
                .bool("puedeRegistrarAjuste", BooleanCriteria.of(filter.puedeRegistrarAjuste()))
                .bool("puedeConsultarKardex", BooleanCriteria.of(filter.puedeConsultarKardex()))
                .bool("puedeGestionarImagenes", BooleanCriteria.of(filter.puedeGestionarImagenes()))
                .bool("puedeActualizarAtributos", BooleanCriteria.of(filter.puedeActualizarAtributos()))
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", SpecificationFilterSupport.dateRange(filter.fechaCreacion()))
                .and(overlapsVigencia(filter.fechaVigencia()))
                .build();
    }

    public static Specification<EmpleadoInventarioPermisoHistorial> activeOnly() {
        return SpecificationBuilder.<EmpleadoInventarioPermisoHistorial>create()
                .activeOnly()
                .build();
    }

    public static Specification<EmpleadoInventarioPermisoHistorial> vigentesOnly() {
        return SpecificationBuilder.<EmpleadoInventarioPermisoHistorial>create()
                .activeOnly()
                .equal("vigente", Boolean.TRUE)
                .build();
    }

    public static Specification<EmpleadoInventarioPermisoHistorial> byUsuarioMs1(Long idUsuarioMs1) {
        return SpecificationBuilder.<EmpleadoInventarioPermisoHistorial>create()
                .equal("empleadoSnapshot.idUsuarioMs1", idUsuarioMs1)
                .build();
    }

    public static Specification<EmpleadoInventarioPermisoHistorial> byEmpleadoSnapshot(Long idEmpleadoSnapshot) {
        return SpecificationBuilder.<EmpleadoInventarioPermisoHistorial>create()
                .equal("empleadoSnapshot.idEmpleadoSnapshot", idEmpleadoSnapshot)
                .build();
    }

    public static Specification<EmpleadoInventarioPermisoHistorial> overlapsVigencia(DateRangeFilterDto filter) {
        DateRangeCriteria<LocalDateTime> range = SpecificationFilterSupport.dateRange(filter);

        return (root, query, cb) -> {
            if (range == null || !range.hasAny()) {
                return cb.conjunction();
            }

            if (range.hasFrom() && range.hasTo()) {
                return cb.and(
                        cb.lessThanOrEqualTo(root.get("fechaInicio"), range.to()),
                        cb.or(
                                cb.isNull(root.get("fechaFin")),
                                cb.greaterThanOrEqualTo(root.get("fechaFin"), range.from())
                        )
                );
            }

            if (range.hasFrom()) {
                return cb.or(
                        cb.isNull(root.get("fechaFin")),
                        cb.greaterThanOrEqualTo(root.get("fechaFin"), range.from())
                );
            }

            return cb.lessThanOrEqualTo(root.get("fechaInicio"), range.to());
        };
    }
}