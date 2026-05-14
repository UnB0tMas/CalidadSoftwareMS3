// ruta: src/main/java/com/upsjb/ms3/specification/ProductoImagenSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.ProductoImagenCloudinary;
import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoImagenFilterDto;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.DateRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class ProductoImagenSpecifications {

    private ProductoImagenSpecifications() {
    }

    public static Specification<ProductoImagenCloudinary> fromFilter(ProductoImagenFilterDto filter) {
        if (filter == null) {
            return activeOnly();
        }

        Specification<ProductoImagenCloudinary> specification = SpecificationBuilder.<ProductoImagenCloudinary>create()
                .textSearch(
                        filter.search(),
                        "cloudinaryPublicId",
                        "cloudinaryAssetId",
                        "secureUrl",
                        "originalFilename",
                        "altText",
                        "titulo",
                        "producto.codigoProducto",
                        "producto.slug",
                        "producto.nombre",
                        "sku.codigoSku",
                        "sku.barcode"
                )
                .equal("idImagen", filter.idImagen())
                .equal("producto.idProducto", filter.idProducto())
                .like("producto.codigoProducto", filter.codigoProducto())
                .like("producto.slug", filter.slugProducto())
                .equal("sku.idSku", filter.idSku())
                .like("sku.codigoSku", filter.codigoSku())
                .like("sku.barcode", filter.barcode())
                .like("cloudinaryPublicId", filter.cloudinaryPublicId())
                .like("cloudinaryAssetId", filter.cloudinaryAssetId())
                .like("resourceType", filter.resourceType())
                .like("format", filter.format())
                .equal("principal", filter.principal())
                .equal("creadoPorIdUsuarioMs1", filter.creadoPorIdUsuarioMs1())
                .bool("estado", BooleanCriteria.of(filter.estado() == null ? Boolean.TRUE : filter.estado()))
                .range("createdAt", toDateTimeRange(filter.fechaCreacion()))
                .build();

        if (Boolean.TRUE.equals(filter.soloProductoBase())) {
            specification = specification.and(byProductoBase(filter.idProducto()));
        }

        if (Boolean.TRUE.equals(filter.soloSku())) {
            specification = specification.and((root, query, cb) -> cb.isNotNull(root.get("sku")));
        }

        return specification;
    }

    public static Specification<ProductoImagenCloudinary> activeOnly() {
        return SpecificationBuilder.<ProductoImagenCloudinary>create()
                .activeOnly()
                .build();
    }

    public static Specification<ProductoImagenCloudinary> byProducto(Long idProducto) {
        return SpecificationBuilder.<ProductoImagenCloudinary>create()
                .equal("producto.idProducto", idProducto)
                .build();
    }

    public static Specification<ProductoImagenCloudinary> bySku(Long idSku) {
        return SpecificationBuilder.<ProductoImagenCloudinary>create()
                .equal("sku.idSku", idSku)
                .build();
    }

    public static Specification<ProductoImagenCloudinary> byProductoBase(Long idProducto) {
        return (root, query, cb) -> idProducto == null
                ? cb.isNull(root.get("sku"))
                : cb.and(
                cb.equal(root.get("producto").get("idProducto"), idProducto),
                cb.isNull(root.get("sku"))
        );
    }

    public static Specification<ProductoImagenCloudinary> principalesOnly() {
        return SpecificationBuilder.<ProductoImagenCloudinary>create()
                .equal("principal", Boolean.TRUE)
                .build();
    }

    public static Specification<ProductoImagenCloudinary> byCloudinaryPublicId(String publicId) {
        return SpecificationBuilder.<ProductoImagenCloudinary>create()
                .like("cloudinaryPublicId", publicId)
                .build();
    }

    public static Specification<ProductoImagenCloudinary> search(String search) {
        return SpecificationBuilder.<ProductoImagenCloudinary>create()
                .textSearch(
                        search,
                        "cloudinaryPublicId",
                        "cloudinaryAssetId",
                        "secureUrl",
                        "originalFilename",
                        "altText",
                        "titulo",
                        "producto.codigoProducto",
                        "producto.nombre",
                        "sku.codigoSku"
                )
                .build();
    }

    public static Specification<ProductoImagenCloudinary> estado(Boolean estado) {
        return SpecificationBuilder.<ProductoImagenCloudinary>create()
                .bool("estado", BooleanCriteria.of(estado == null ? Boolean.TRUE : estado))
                .build();
    }

    public static Specification<ProductoImagenCloudinary> createdBetween(DateRangeFilterDto range) {
        return SpecificationBuilder.<ProductoImagenCloudinary>create()
                .range("createdAt", toDateTimeRange(range))
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