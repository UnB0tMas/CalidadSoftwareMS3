package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.CategoriaAtributo;
import com.upsjb.ms3.dto.catalogo.atributo.filter.CategoriaAtributoFilterDto;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.DateRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class CategoriaAtributoSpecifications {

    private CategoriaAtributoSpecifications() {
    }

    public static Specification<CategoriaAtributo> fromFilter(
            CategoriaAtributoFilterDto filter
    ) {
        if (filter == null) {
            return SpecificationBuilder.<CategoriaAtributo>create()
                    .activeOnly()
                    .build();
        }

        Long idCategoria = filter.idCategoria() != null
                ? filter.idCategoria()
                : filter.categoria() == null ? null : filter.categoria().id();

        Long idAtributo = filter.idAtributo() != null
                ? filter.idAtributo()
                : filter.atributo() == null ? null : filter.atributo().id();

        return SpecificationBuilder.<CategoriaAtributo>create()
                .textSearch(
                        filter.search(),
                        "categoria.codigo",
                        "categoria.nombre",
                        "atributo.codigo",
                        "atributo.nombre"
                )
                .equal("categoria.idCategoria", idCategoria)
                .equal("atributo.idAtributo", idAtributo)
                .like("categoria.codigo", filter.codigoCategoria())
                .like("categoria.nombre", filter.nombreCategoria())
                .like("atributo.codigo", filter.codigoAtributo())
                .like("atributo.nombre", filter.nombreAtributo())
                .equal("atributo.tipoDato", filter.tipoDato())
                .bool("requerido", BooleanCriteria.of(filter.requerido()))
                .bool("atributo.requerido", BooleanCriteria.of(filter.atributoRequeridoBase()))
                .bool("atributo.filtrable", BooleanCriteria.of(filter.filtrable()))
                .bool("atributo.visiblePublico", BooleanCriteria.of(filter.visiblePublico()))
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", toDateTimeRange(filter.fechaCreacion()))
                .build();
    }

    public static Specification<CategoriaAtributo> activeOnly() {
        return SpecificationBuilder.<CategoriaAtributo>create()
                .activeOnly()
                .build();
    }

    public static Specification<CategoriaAtributo> byCategoria(Long idCategoria) {
        return SpecificationBuilder.<CategoriaAtributo>create()
                .equal("categoria.idCategoria", idCategoria)
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