// ruta: src/main/java/com/upsjb/ms3/specification/ProductoSkuSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoSkuFilterDto;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.DateRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class ProductoSkuSpecifications {

    private ProductoSkuSpecifications() {
    }

    public static Specification<ProductoSku> fromFilter(ProductoSkuFilterDto filter) {
        if (filter == null) {
            return SpecificationBuilder.<ProductoSku>create()
                    .activeOnly()
                    .build();
        }

        return SpecificationBuilder.<ProductoSku>create()
                .textSearch(
                        filter.search(),
                        "codigoSku",
                        "barcode",
                        "color",
                        "talla",
                        "material",
                        "modelo",
                        "producto.codigoProducto",
                        "producto.nombre"
                )
                .equal("producto.idProducto", filter.idProducto())
                .like("producto.codigoProducto", filter.codigoProducto())
                .like("codigoSku", filter.codigoSku())
                .like("barcode", filter.barcode())
                .like("color", filter.color())
                .like("talla", filter.talla())
                .like("material", filter.material())
                .like("modelo", filter.modelo())
                .equal("estadoSku", filter.estadoSku())
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", toDateTimeRange(filter.fechaCreacion()))
                .build();
    }

    public static Specification<ProductoSku> activeOnly() {
        return SpecificationBuilder.<ProductoSku>create()
                .activeOnly()
                .build();
    }

    public static Specification<ProductoSku> byProducto(Long idProducto) {
        return SpecificationBuilder.<ProductoSku>create()
                .equal("producto.idProducto", idProducto)
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