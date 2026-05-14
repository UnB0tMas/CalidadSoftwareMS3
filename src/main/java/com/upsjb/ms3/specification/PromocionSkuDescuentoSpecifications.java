// ruta: src/main/java/com/upsjb/ms3/specification/PromocionSkuDescuentoSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.PromocionSkuDescuentoVersion;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.domain.enums.TipoDescuento;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.NumericRangeCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class PromocionSkuDescuentoSpecifications {

    private PromocionSkuDescuentoSpecifications() {
    }

    public static Specification<PromocionSkuDescuentoVersion> activeOnly() {
        return SpecificationBuilder.<PromocionSkuDescuentoVersion>create()
                .activeOnly()
                .build();
    }

    public static Specification<PromocionSkuDescuentoVersion> byPromocionVersion(Long idPromocionVersion) {
        return SpecificationBuilder.<PromocionSkuDescuentoVersion>create()
                .equal("promocionVersion.idPromocionVersion", idPromocionVersion)
                .build();
    }

    public static Specification<PromocionSkuDescuentoVersion> byPromocion(Long idPromocion) {
        return SpecificationBuilder.<PromocionSkuDescuentoVersion>create()
                .equal("promocionVersion.promocion.idPromocion", idPromocion)
                .build();
    }

    public static Specification<PromocionSkuDescuentoVersion> bySku(Long idSku) {
        return SpecificationBuilder.<PromocionSkuDescuentoVersion>create()
                .equal("sku.idSku", idSku)
                .build();
    }

    public static Specification<PromocionSkuDescuentoVersion> byProducto(Long idProducto) {
        return SpecificationBuilder.<PromocionSkuDescuentoVersion>create()
                .equal("sku.producto.idProducto", idProducto)
                .build();
    }

    public static Specification<PromocionSkuDescuentoVersion> byTipoDescuento(TipoDescuento tipoDescuento) {
        return SpecificationBuilder.<PromocionSkuDescuentoVersion>create()
                .equal("tipoDescuento", tipoDescuento)
                .build();
    }

    public static Specification<PromocionSkuDescuentoVersion> search(String search) {
        return SpecificationBuilder.<PromocionSkuDescuentoVersion>create()
                .textSearch(
                        search,
                        "promocionVersion.promocion.codigo",
                        "promocionVersion.promocion.nombre",
                        "sku.codigoSku",
                        "sku.producto.codigoProducto",
                        "sku.producto.nombre"
                )
                .build();
    }

    public static Specification<PromocionSkuDescuentoVersion> estado(Boolean estado) {
        return SpecificationBuilder.<PromocionSkuDescuentoVersion>create()
                .bool("estado", BooleanCriteria.of(estado == null ? Boolean.TRUE : estado))
                .build();
    }

    public static Specification<PromocionSkuDescuentoVersion> valorDescuentoBetween(BigDecimal min, BigDecimal max) {
        return SpecificationBuilder.<PromocionSkuDescuentoVersion>create()
                .numericRange("valorDescuento", new NumericRangeCriteria<>(min, max))
                .build();
    }

    public static Specification<PromocionSkuDescuentoVersion> publicApplicableAt(LocalDateTime dateTime) {
        LocalDateTime resolvedDateTime = dateTime == null ? LocalDateTime.now() : dateTime;

        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("estado")),
                cb.isTrue(root.get("promocionVersion").get("estado")),
                cb.isTrue(root.get("promocionVersion").get("vigente")),
                cb.isTrue(root.get("promocionVersion").get("visiblePublico")),
                root.get("promocionVersion").get("estadoPromocion").in(
                        EstadoPromocion.ACTIVA,
                        EstadoPromocion.PROGRAMADA
                ),
                cb.lessThanOrEqualTo(root.get("promocionVersion").get("fechaInicio"), resolvedDateTime),
                cb.greaterThanOrEqualTo(root.get("promocionVersion").get("fechaFin"), resolvedDateTime)
        );
    }
}