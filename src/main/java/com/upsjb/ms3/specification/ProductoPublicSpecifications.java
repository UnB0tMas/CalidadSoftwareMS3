// ruta: src/main/java/com/upsjb/ms3/specification/ProductoPublicSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.PrecioSkuHistorial;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.PromocionSkuDescuentoVersion;
import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.domain.enums.EstadoSku;
import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoPublicFilterDto;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import com.upsjb.ms3.util.DateTimeUtil;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ProductoPublicSpecifications {

    private ProductoPublicSpecifications() {
    }

    public static Specification<Producto> fromFilter(ProductoPublicFilterDto filter) {
        LocalDateTime now = DateTimeUtil.nowUtc();

        Specification<Producto> spec = basePublic(
                filter != null && Boolean.TRUE.equals(filter.incluirProgramados()),
                now
        );

        if (filter == null) {
            return spec;
        }

        spec = spec.and(SpecificationBuilder.<Producto>create()
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
                        "marca.codigo"
                )
                .like("categoria.slug", filter.categoriaSlug())
                .like("marca.slug", filter.marcaSlug())
                .equal("generoObjetivo", filter.generoObjetivo())
                .like("temporada", filter.temporada())
                .like("deporte", filter.deporte())
                .build());

        if (Boolean.TRUE.equals(filter.soloVendibles())) {
            spec = spec.and(vendiblesOnly());
        }

        if (Boolean.TRUE.equals(filter.conPromocion())) {
            spec = spec.and(withPublicPromotion(now));
        }

        if (filter.precioMin() != null || filter.precioMax() != null) {
            spec = spec.and(withCurrentPriceBetween(filter.precioMin(), filter.precioMax(), now));
        }

        return spec;
    }

    public static Specification<Producto> basePublic(boolean incluirProgramados, LocalDateTime now) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            predicates.add(cb.isTrue(root.get("estado")));
            predicates.add(cb.isTrue(root.get("visiblePublico")));
            predicates.add(cb.equal(root.get("estadoRegistro"), EstadoProductoRegistro.ACTIVO));
            predicates.add(root.get("estadoVenta").in(
                    EstadoProductoVenta.VENDIBLE,
                    EstadoProductoVenta.SOLO_VISIBLE,
                    EstadoProductoVenta.AGOTADO,
                    EstadoProductoVenta.PROXIMAMENTE
            ));

            var publicadoVigente = cb.and(
                    cb.equal(root.get("estadoPublicacion"), EstadoProductoPublicacion.PUBLICADO),
                    cb.or(
                            cb.isNull(root.get("fechaPublicacionInicio")),
                            cb.lessThanOrEqualTo(root.get("fechaPublicacionInicio"), now)
                    ),
                    cb.or(
                            cb.isNull(root.get("fechaPublicacionFin")),
                            cb.greaterThanOrEqualTo(root.get("fechaPublicacionFin"), now)
                    )
            );

            if (incluirProgramados) {
                var programadoVisible = cb.and(
                        cb.equal(root.get("estadoPublicacion"), EstadoProductoPublicacion.PROGRAMADO),
                        cb.or(
                                cb.isNull(root.get("fechaPublicacionFin")),
                                cb.greaterThanOrEqualTo(root.get("fechaPublicacionFin"), now)
                        )
                );

                predicates.add(cb.or(publicadoVigente, programadoVisible));
            } else {
                predicates.add(publicadoVigente);
            }

            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    public static Specification<Producto> vendiblesOnly() {
        return SpecificationBuilder.<Producto>create()
                .equal("estadoVenta", EstadoProductoVenta.VENDIBLE)
                .equal("vendible", Boolean.TRUE)
                .build()
                .and(withActiveSku());
    }

    public static Specification<Producto> bySlug(String slug) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(slug)) {
                return cb.conjunction();
            }

            return cb.equal(cb.lower(root.get("slug")), slug.trim().toLowerCase(java.util.Locale.ROOT));
        };
    }

    public static Specification<Producto> withActiveSku() {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ProductoSku> sku = subquery.from(ProductoSku.class);

            subquery.select(sku.get("producto").get("idProducto"))
                    .where(
                            cb.equal(sku.get("producto").get("idProducto"), root.get("idProducto")),
                            cb.isTrue(sku.get("estado")),
                            cb.equal(sku.get("estadoSku"), EstadoSku.ACTIVO)
                    );

            return cb.exists(subquery);
        };
    }

    public static Specification<Producto> withCurrentPriceBetween(
            BigDecimal min,
            BigDecimal max,
            LocalDateTime now
    ) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<PrecioSkuHistorial> precio = subquery.from(PrecioSkuHistorial.class);

            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            predicates.add(cb.equal(precio.get("sku").get("producto").get("idProducto"), root.get("idProducto")));
            predicates.add(cb.isTrue(precio.get("estado")));
            predicates.add(cb.isTrue(precio.get("vigente")));
            predicates.add(cb.lessThanOrEqualTo(precio.get("fechaInicio"), now));
            predicates.add(cb.or(
                    cb.isNull(precio.get("fechaFin")),
                    cb.greaterThanOrEqualTo(precio.get("fechaFin"), now)
            ));

            if (min != null) {
                predicates.add(cb.greaterThanOrEqualTo(precio.get("precioVenta"), min));
            }

            if (max != null) {
                predicates.add(cb.lessThanOrEqualTo(precio.get("precioVenta"), max));
            }

            subquery.select(precio.get("sku").get("producto").get("idProducto"))
                    .where(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));

            return cb.exists(subquery);
        };
    }

    public static Specification<Producto> withPublicPromotion(LocalDateTime now) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<PromocionSkuDescuentoVersion> descuento = subquery.from(PromocionSkuDescuentoVersion.class);

            subquery.select(descuento.get("sku").get("producto").get("idProducto"))
                    .where(
                            cb.equal(descuento.get("sku").get("producto").get("idProducto"), root.get("idProducto")),
                            cb.isTrue(descuento.get("estado")),
                            cb.isTrue(descuento.get("promocionVersion").get("estado")),
                            cb.isTrue(descuento.get("promocionVersion").get("vigente")),
                            cb.isTrue(descuento.get("promocionVersion").get("visiblePublico")),
                            descuento.get("promocionVersion").get("estadoPromocion").in(
                                    EstadoPromocion.ACTIVA,
                                    EstadoPromocion.PROGRAMADA
                            ),
                            cb.lessThanOrEqualTo(descuento.get("promocionVersion").get("fechaInicio"), now),
                            cb.greaterThanOrEqualTo(descuento.get("promocionVersion").get("fechaFin"), now)
                    );

            return cb.exists(subquery);
        };
    }
}