// ruta: src/main/java/com/upsjb/ms3/specification/EmpleadoSnapshotMs2Specifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.EmpleadoSnapshotMs2;
import com.upsjb.ms3.dto.empleado.filter.EmpleadoInventarioPermisoFilterDto;
import com.upsjb.ms3.dto.empleado.filter.EmpleadoSnapshotMs2FilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;

public final class EmpleadoSnapshotMs2Specifications {

    private EmpleadoSnapshotMs2Specifications() {
    }

    public static Specification<EmpleadoSnapshotMs2> fromFilter(EmpleadoSnapshotMs2FilterDto filter) {
        if (filter == null) {
            return activeOnly();
        }

        return SpecificationBuilder.<EmpleadoSnapshotMs2>create()
                .textSearch(
                        filter.search(),
                        "codigoEmpleado",
                        "nombresCompletos",
                        "areaCodigo",
                        "areaNombre"
                )
                .equal("idEmpleadoSnapshot", filter.idEmpleadoSnapshot())
                .equal("idEmpleadoMs2", filter.idEmpleadoMs2())
                .equal("idUsuarioMs1", filter.idUsuarioMs1())
                .like("codigoEmpleado", filter.codigoEmpleado())
                .like("nombresCompletos", filter.nombresCompletos())
                .like("areaCodigo", filter.areaCodigo())
                .like("areaNombre", filter.areaNombre())
                .equal("snapshotVersion", filter.snapshotVersion())
                .bool("empleadoActivo", BooleanCriteria.of(filter.empleadoActivo()))
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("snapshotAt", SpecificationFilterSupport.dateRange(filter.snapshotAt()))
                .range("createdAt", SpecificationFilterSupport.dateRange(filter.fechaCreacion()))
                .build();
    }

    public static Specification<EmpleadoSnapshotMs2> fromPermisoFilter(EmpleadoInventarioPermisoFilterDto filter) {
        if (filter == null) {
            return activeOnly();
        }

        return SpecificationBuilder.<EmpleadoSnapshotMs2>create()
                .textSearch(
                        filter.search(),
                        "codigoEmpleado",
                        "nombresCompletos",
                        "areaCodigo",
                        "areaNombre"
                )
                .equal("idEmpleadoSnapshot", filter.idEmpleadoSnapshot())
                .equal("idEmpleadoMs2", filter.idEmpleadoMs2())
                .equal("idUsuarioMs1", filter.idUsuarioMs1())
                .like("codigoEmpleado", filter.codigoEmpleado())
                .like("areaCodigo", filter.areaCodigo())
                .bool("empleadoActivo", BooleanCriteria.of(filter.empleadoActivo()))
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", SpecificationFilterSupport.dateRange(filter.fechaCreacion()))
                .build();
    }

    public static Specification<EmpleadoSnapshotMs2> activeOnly() {
        return SpecificationBuilder.<EmpleadoSnapshotMs2>create()
                .activeOnly()
                .build();
    }

    public static Specification<EmpleadoSnapshotMs2> activeEmployeeOnly() {
        return SpecificationBuilder.<EmpleadoSnapshotMs2>create()
                .activeOnly()
                .equal("empleadoActivo", Boolean.TRUE)
                .build();
    }

    public static Specification<EmpleadoSnapshotMs2> byUsuarioMs1(Long idUsuarioMs1) {
        return SpecificationBuilder.<EmpleadoSnapshotMs2>create()
                .equal("idUsuarioMs1", idUsuarioMs1)
                .build();
    }

    public static Specification<EmpleadoSnapshotMs2> byEmpleadoMs2(Long idEmpleadoMs2) {
        return SpecificationBuilder.<EmpleadoSnapshotMs2>create()
                .equal("idEmpleadoMs2", idEmpleadoMs2)
                .build();
    }

    public static Specification<EmpleadoSnapshotMs2> byCodigoEmpleado(String codigoEmpleado) {
        return SpecificationBuilder.<EmpleadoSnapshotMs2>create()
                .like("codigoEmpleado", codigoEmpleado)
                .build();
    }
}