// ruta: src/main/java/com/upsjb/ms3/specification/ProductoSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoFilterDto;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.DateRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class ProductoSpecifications {

    private ProductoSpecifications() {
    }

    public static Specification<Producto> fromFilter(ProductoFilterDto filter) {
        if (filter == null) {
            return SpecificationBuilder.<Producto>create()
                    .activeOnly()
                    .build();
        }

        return SpecificationBuilder.<Producto>create()
                .textSearch(
                        filter.search(),
                        "codigoProducto",
                        "nombre",
                        "slug",
                        "descripcionCorta",
                        "descripcionLarga",
                        "temporada",
                        "deporte",
                        "categoria.nombre",
                        "categoria.codigo",
                        "marca.nombre",
                        "marca.codigo",
                        "tipoProducto.nombre",
                        "tipoProducto.codigo"
                )
                .like("codigoProducto", filter.codigoProducto())
                .like("nombre", filter.nombre())
                .like("slug", filter.slug())
                .equal("tipoProducto.idTipoProducto", filter.idTipoProducto())
                .equal("categoria.idCategoria", filter.idCategoria())
                .equal("marca.idMarca", filter.idMarca())
                .equal("generoObjetivo", filter.generoObjetivo())
                .like("temporada", filter.temporada())
                .like("deporte", filter.deporte())
                .equal("estadoRegistro", filter.estadoRegistro())
                .equal("estadoPublicacion", filter.estadoPublicacion())
                .equal("estadoVenta", filter.estadoVenta())
                .bool("visiblePublico", BooleanCriteria.of(filter.visiblePublico()))
                .bool("vendible", BooleanCriteria.of(filter.vendible()))
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", toDateTimeRange(filter.fechaCreacion()))
                .build();
    }

    public static Specification<Producto> activeOnly() {
        return SpecificationBuilder.<Producto>create()
                .activeOnly()
                .build();
    }

    public static Specification<Producto> byCategoria(Long idCategoria) {
        return SpecificationBuilder.<Producto>create()
                .equal("categoria.idCategoria", idCategoria)
                .build();
    }

    public static Specification<Producto> byMarca(Long idMarca) {
        return SpecificationBuilder.<Producto>create()
                .equal("marca.idMarca", idMarca)
                .build();
    }

    public static Specification<Producto> byTipoProducto(Long idTipoProducto) {
        return SpecificationBuilder.<Producto>create()
                .equal("tipoProducto.idTipoProducto", idTipoProducto)
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