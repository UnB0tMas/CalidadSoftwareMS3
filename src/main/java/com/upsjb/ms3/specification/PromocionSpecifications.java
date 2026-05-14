// ruta: src/main/java/com/upsjb/ms3/specification/PromocionSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.Promocion;
import com.upsjb.ms3.dto.promocion.filter.PromocionFilterDto;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.DateRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class PromocionSpecifications {

    private PromocionSpecifications() {
    }

    public static Specification<Promocion> fromFilter(PromocionFilterDto filter) {
        if (filter == null) {
            return SpecificationBuilder.<Promocion>create()
                    .activeOnly()
                    .build();
        }

        return SpecificationBuilder.<Promocion>create()
                .textSearch(filter.search(), "codigo", "nombre", "descripcion")
                .like("codigo", filter.codigo())
                .like("nombre", filter.nombre())
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", toDateTimeRange(filter.fechaCreacion()))
                .build();
    }

    public static Specification<Promocion> activeOnly() {
        return SpecificationBuilder.<Promocion>create()
                .activeOnly()
                .build();
    }

    public static Specification<Promocion> byCodigo(String codigo) {
        return SpecificationBuilder.<Promocion>create()
                .like("codigo", codigo)
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