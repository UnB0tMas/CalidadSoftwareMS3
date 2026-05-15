// ruta: src/main/java/com/upsjb/ms3/specification/TipoProductoAtributoSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.TipoProductoAtributo;
import com.upsjb.ms3.dto.catalogo.atributo.filter.TipoProductoAtributoFilterDto;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.DateRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import com.upsjb.ms3.util.StringNormalizer;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class TipoProductoAtributoSpecifications {

    private TipoProductoAtributoSpecifications() {
    }

    public static Specification<TipoProductoAtributo> fromFilter(TipoProductoAtributoFilterDto filter) {
        if (filter == null) {
            return activeOnly();
        }

        EntityReferenceDto tipoProducto = filter.tipoProducto();
        EntityReferenceDto atributo = filter.atributo();

        return SpecificationBuilder.<TipoProductoAtributo>create()
                .textSearch(
                        filter.search(),
                        "tipoProducto.codigo",
                        "tipoProducto.nombre",
                        "atributo.codigo",
                        "atributo.nombre",
                        "atributo.unidadMedida"
                )
                .equal("tipoProducto.idTipoProducto", firstLong(filter.idTipoProducto(), tipoProducto == null ? null : tipoProducto.id()))
                .equal("atributo.idAtributo", firstLong(filter.idAtributo(), atributo == null ? null : atributo.id()))
                .like("tipoProducto.codigo", firstText(filter.codigoTipoProducto(), tipoProducto == null ? null : tipoProducto.codigo()))
                .like("tipoProducto.nombre", firstText(filter.nombreTipoProducto(), tipoProducto == null ? null : tipoProducto.nombre()))
                .like("atributo.codigo", firstText(filter.codigoAtributo(), atributo == null ? null : atributo.codigo()))
                .like("atributo.nombre", firstText(filter.nombreAtributo(), atributo == null ? null : atributo.nombre()))
                .equal("atributo.tipoDato", filter.tipoDato())
                .bool("requerido", BooleanCriteria.of(filter.requerido()))
                .bool("atributo.requerido", BooleanCriteria.of(filter.atributoRequeridoBase()))
                .bool("atributo.filtrable", BooleanCriteria.of(filter.filtrable()))
                .bool("atributo.visiblePublico", BooleanCriteria.of(filter.visiblePublico()))
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", toDateTimeRange(filter.fechaCreacion()))
                .build();
    }

    public static Specification<TipoProductoAtributo> activeOnly() {
        return SpecificationBuilder.<TipoProductoAtributo>create()
                .activeOnly()
                .build();
    }

    public static Specification<TipoProductoAtributo> byTipoProducto(Long idTipoProducto) {
        return SpecificationBuilder.<TipoProductoAtributo>create()
                .activeOnly()
                .equal("tipoProducto.idTipoProducto", idTipoProducto)
                .build();
    }

    private static String firstText(String first, String second) {
        return StringNormalizer.hasText(first) ? first : second;
    }

    private static Long firstLong(Long first, Long second) {
        return first != null ? first : second;
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