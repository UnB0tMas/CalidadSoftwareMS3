// ruta: src/main/java/com/upsjb/ms3/specification/PrecioSkuSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.PrecioSkuHistorial;
import com.upsjb.ms3.dto.precio.filter.PrecioSkuFilterDto;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.DateRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class PrecioSkuSpecifications {

    private PrecioSkuSpecifications() {
    }

    public static Specification<PrecioSkuHistorial> fromFilter(PrecioSkuFilterDto filter) {
        if (filter == null) {
            return SpecificationBuilder.<PrecioSkuHistorial>create()
                    .activeOnly()
                    .build();
        }

        return SpecificationBuilder.<PrecioSkuHistorial>create()
                .equal("sku.idSku", filter.idSku())
                .like("sku.codigoSku", filter.codigoSku())
                .equal("sku.producto.idProducto", filter.idProducto())
                .like("sku.producto.codigoProducto", filter.codigoProducto())
                .equal("moneda", filter.moneda())
                .bool("vigente", BooleanCriteria.of(filter.vigente()))
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("fechaInicio", toDateTimeRange(filter.fechaInicio()))
                .range("createdAt", toDateTimeRange(filter.fechaCreacion()))
                .build();
    }

    public static Specification<PrecioSkuHistorial> vigente() {
        return SpecificationBuilder.<PrecioSkuHistorial>create()
                .activeOnly()
                .equal("vigente", Boolean.TRUE)
                .build();
    }

    public static Specification<PrecioSkuHistorial> bySku(Long idSku) {
        return SpecificationBuilder.<PrecioSkuHistorial>create()
                .equal("sku.idSku", idSku)
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