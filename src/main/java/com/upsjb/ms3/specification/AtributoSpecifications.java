// ruta: src/main/java/com/upsjb/ms3/specification/AtributoSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.dto.catalogo.atributo.filter.AtributoFilterDto;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.DateRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class AtributoSpecifications {

    private AtributoSpecifications() {
    }

    public static Specification<Atributo> fromFilter(AtributoFilterDto filter) {
        if (filter == null) {
            return SpecificationBuilder.<Atributo>create()
                    .activeOnly()
                    .build();
        }

        return SpecificationBuilder.<Atributo>create()
                .textSearch(filter.search(), "codigo", "nombre", "unidadMedida")
                .like("codigo", filter.codigo())
                .like("nombre", filter.nombre())
                .equal("tipoDato", filter.tipoDato())
                .bool("requerido", BooleanCriteria.of(filter.requerido()))
                .bool("filtrable", BooleanCriteria.of(filter.filtrable()))
                .bool("visiblePublico", BooleanCriteria.of(filter.visiblePublico()))
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", toDateTimeRange(filter.fechaCreacion()))
                .build();
    }

    public static Specification<Atributo> activeOnly() {
        return SpecificationBuilder.<Atributo>create()
                .activeOnly()
                .build();
    }

    public static Specification<Atributo> publicVisible() {
        return SpecificationBuilder.<Atributo>create()
                .activeOnly()
                .equal("visiblePublico", Boolean.TRUE)
                .build();
    }

    public static Specification<Atributo> filtrable() {
        return SpecificationBuilder.<Atributo>create()
                .activeOnly()
                .equal("filtrable", Boolean.TRUE)
                .build();
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
                // Se intenta con el siguiente nombre soportado.
            }
        }

        return null;
    }
}