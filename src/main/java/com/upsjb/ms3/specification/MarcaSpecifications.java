// ruta: src/main/java/com/upsjb/ms3/specification/MarcaSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.dto.catalogo.marca.filter.MarcaFilterDto;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.DateRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class MarcaSpecifications {

    private MarcaSpecifications() {
    }

    public static Specification<Marca> fromFilter(MarcaFilterDto filter) {
        if (filter == null) {
            return SpecificationBuilder.<Marca>create()
                    .activeOnly()
                    .build();
        }

        return SpecificationBuilder.<Marca>create()
                .textSearch(filter.search(), "codigo", "nombre", "slug", "descripcion")
                .like("codigo", filter.codigo())
                .like("nombre", filter.nombre())
                .like("slug", filter.slug())
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", toDateTimeRange(filter.fechaCreacion()))
                .build();
    }

    public static Specification<Marca> activeOnly() {
        return SpecificationBuilder.<Marca>create()
                .activeOnly()
                .build();
    }

    public static Specification<Marca> bySlug(String slug) {
        return SpecificationBuilder.<Marca>create()
                .like("slug", slug)
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