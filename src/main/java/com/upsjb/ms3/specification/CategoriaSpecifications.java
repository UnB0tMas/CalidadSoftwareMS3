// ruta: src/main/java/com/upsjb/ms3/specification/CategoriaSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.dto.catalogo.categoria.filter.CategoriaFilterDto;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.DateRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import jakarta.persistence.criteria.JoinType;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class CategoriaSpecifications {

    private CategoriaSpecifications() {
    }

    public static Specification<Categoria> fromFilter(CategoriaFilterDto filter) {
        if (filter == null) {
            return SpecificationBuilder.<Categoria>create()
                    .activeOnly()
                    .build();
        }

        return SpecificationBuilder.<Categoria>create()
                .textSearch(filter.search(), "codigo", "nombre", "slug", "descripcion")
                .like("codigo", filter.codigo())
                .like("nombre", filter.nombre())
                .like("slug", filter.slug())
                .equal("categoriaPadre.idCategoria", filter.idCategoriaPadre())
                .equal("nivel", filter.nivel())
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", toDateTimeRange(filter.fechaCreacion()))
                .build();
    }

    public static Specification<Categoria> rootCategories() {
        return (root, query, cb) -> cb.isNull(root.get("categoriaPadre"));
    }

    public static Specification<Categoria> byParent(Long idCategoriaPadre) {
        return (root, query, cb) -> idCategoriaPadre == null
                ? cb.conjunction()
                : cb.equal(root.join("categoriaPadre", JoinType.LEFT).get("idCategoria"), idCategoriaPadre);
    }

    public static Specification<Categoria> activeOnly() {
        return SpecificationBuilder.<Categoria>create()
                .activeOnly()
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