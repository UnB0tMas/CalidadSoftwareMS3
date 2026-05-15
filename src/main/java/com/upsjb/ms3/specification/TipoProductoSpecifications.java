// ruta: src/main/java/com/upsjb/ms3/specification/TipoProductoSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.filter.TipoProductoFilterDto;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.DateRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class TipoProductoSpecifications {

    private TipoProductoSpecifications() {
    }

    public static Specification<TipoProducto> fromFilter(TipoProductoFilterDto filter) {
        if (filter == null) {
            return SpecificationBuilder.<TipoProducto>create()
                    .activeOnly()
                    .build();
        }

        return SpecificationBuilder.<TipoProducto>create()
                .textSearch(filter.search(), "codigo", "nombre", "descripcion")
                .like("codigo", filter.codigo())
                .like("nombre", filter.nombre())
                .bool("estado", BooleanCriteria.of(resolveEstado(filter)))
                .range("createdAt", toDateTimeRange(filter.fechaCreacion()))
                .range("updatedAt", toDateTimeRange(filter.fechaActualizacion()))
                .build();
    }

    public static Specification<TipoProducto> activeOnly() {
        return SpecificationBuilder.<TipoProducto>create()
                .activeOnly()
                .build();
    }

    public static Specification<TipoProducto> byCodigo(String codigo) {
        return SpecificationBuilder.<TipoProducto>create()
                .like("codigo", codigo)
                .build();
    }

    public static Specification<TipoProducto> byNombre(String nombre) {
        return SpecificationBuilder.<TipoProducto>create()
                .like("nombre", nombre)
                .build();
    }

    private static Boolean resolveEstado(TipoProductoFilterDto filter) {
        if (filter == null) {
            return Boolean.TRUE;
        }

        if (Boolean.TRUE.equals(filter.incluirTodosLosEstados())) {
            return null;
        }

        return filter.estado() == null ? Boolean.TRUE : filter.estado();
    }

    private static DateRangeCriteria<LocalDateTime> toDateTimeRange(DateRangeFilterDto filter) {
        if (filter == null) {
            return null;
        }

        return DateRangeCriteria.of(
                readDateTime(filter, "from", "desde", "fechaDesde", "inicio", "start", "fechaInicio"),
                readDateTime(filter, "to", "hasta", "fechaHasta", "fin", "end", "fechaFin")
        );
    }

    private static LocalDateTime readDateTime(DateRangeFilterDto filter, String... methodNames) {
        for (String methodName : methodNames) {
            try {
                Method method = filter.getClass().getMethod(methodName);
                Object value = method.invoke(filter);

                if (value instanceof LocalDateTime dateTime) {
                    return dateTime;
                }
            } catch (ReflectiveOperationException ignored) {
                // Se continúa con el siguiente nombre compatible.
            }
        }

        return null;
    }
}