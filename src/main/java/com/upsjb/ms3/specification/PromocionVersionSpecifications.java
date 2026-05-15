// ruta: src/main/java/com/upsjb/ms3/specification/PromocionVersionSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.PromocionVersion;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.dto.promocion.filter.PromocionVersionFilterDto;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.DateRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class PromocionVersionSpecifications {

    private PromocionVersionSpecifications() {
    }

    public static Specification<PromocionVersion> fromFilter(PromocionVersionFilterDto filter) {
        if (filter == null) {
            return SpecificationBuilder.<PromocionVersion>create()
                    .activeOnly()
                    .build();
        }

        return SpecificationBuilder.<PromocionVersion>create()
                .textSearch(
                        filter.search(),
                        "promocion.codigo",
                        "promocion.nombre",
                        "promocion.descripcion",
                        "motivo"
                )
                .equal("promocion.idPromocion", filter.idPromocion())
                .like("promocion.codigo", filter.codigoPromocion())
                .like("promocion.nombre", filter.nombrePromocion())
                .equal("estadoPromocion", filter.estadoPromocion())
                .bool("visiblePublico", BooleanCriteria.of(filter.visiblePublico()))
                .bool("vigente", BooleanCriteria.of(filter.vigente()))
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", toDateTimeRange(filter.fechaCreacion()))
                .and(overlapsVigencia(filter.vigencia()))
                .build();
    }

    public static Specification<PromocionVersion> publicVisibleAt(LocalDateTime dateTime) {
        LocalDateTime resolvedDateTime = dateTime == null ? LocalDateTime.now() : dateTime;

        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("estado")),
                cb.isTrue(root.get("vigente")),
                cb.isTrue(root.get("visiblePublico")),
                root.get("estadoPromocion").in(EstadoPromocion.ACTIVA, EstadoPromocion.PROGRAMADA),
                cb.lessThanOrEqualTo(root.get("fechaInicio"), resolvedDateTime),
                cb.greaterThanOrEqualTo(root.get("fechaFin"), resolvedDateTime)
        );
    }

    public static Specification<PromocionVersion> overlapsVigencia(DateRangeFilterDto range) {
        DateRangeCriteria<LocalDateTime> criteria = toDateTimeRange(range);

        return (root, query, cb) -> {
            if (criteria == null || !criteria.hasAny()) {
                return cb.conjunction();
            }

            if (criteria.hasFrom() && criteria.hasTo()) {
                return cb.and(
                        cb.lessThanOrEqualTo(root.get("fechaInicio"), criteria.to()),
                        cb.greaterThanOrEqualTo(root.get("fechaFin"), criteria.from())
                );
            }

            if (criteria.hasFrom()) {
                return cb.greaterThanOrEqualTo(root.get("fechaFin"), criteria.from());
            }

            return cb.lessThanOrEqualTo(root.get("fechaInicio"), criteria.to());
        };
    }

    public static Specification<PromocionVersion> vigente() {
        return SpecificationBuilder.<PromocionVersion>create()
                .activeOnly()
                .equal("vigente", Boolean.TRUE)
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