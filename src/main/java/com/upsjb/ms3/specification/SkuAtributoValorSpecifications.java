// ruta: src/main/java/com/upsjb/ms3/specification/SkuAtributoValorSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.SkuAtributoValor;
import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.dto.catalogo.producto.filter.SkuAtributoValorFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;

public final class SkuAtributoValorSpecifications {

    private SkuAtributoValorSpecifications() {
    }

    public static Specification<SkuAtributoValor> fromFilter(SkuAtributoValorFilterDto filter) {
        if (filter == null) {
            return activeOnly();
        }

        return SpecificationBuilder.<SkuAtributoValor>create()
                .textSearch(
                        filter.search(),
                        "sku.codigoSku",
                        "sku.barcode",
                        "sku.producto.codigoProducto",
                        "sku.producto.nombre",
                        "atributo.codigo",
                        "atributo.nombre",
                        "valorTexto"
                )
                .equal("sku.idSku", filter.idSku())
                .like("sku.codigoSku", filter.codigoSku())
                .like("sku.barcode", filter.barcode())
                .equal("sku.producto.idProducto", filter.idProducto())
                .like("sku.producto.codigoProducto", filter.codigoProducto())
                .like("sku.producto.nombre", filter.nombreProducto())
                .equal("atributo.idAtributo", filter.idAtributo())
                .like("atributo.codigo", filter.codigoAtributo())
                .like("atributo.nombre", filter.nombreAtributo())
                .equal("atributo.tipoDato", filter.tipoDato())
                .like("valorTexto", filter.valorTexto())
                .equal("atributo.visiblePublico", filter.visiblePublico())
                .equal("atributo.filtrable", filter.filtrable())
                .bool("estado", BooleanCriteria.of(resolveEstado(filter)))
                .range("createdAt", SpecificationFilterSupport.dateRange(filter.fechaCreacion()))
                .range("updatedAt", SpecificationFilterSupport.dateRange(filter.fechaActualizacion()))
                .build();
    }

    public static Specification<SkuAtributoValor> activeOnly() {
        return SpecificationBuilder.<SkuAtributoValor>create()
                .activeOnly()
                .build();
    }

    public static Specification<SkuAtributoValor> bySku(Long idSku) {
        return SpecificationBuilder.<SkuAtributoValor>create()
                .activeOnly()
                .equal("sku.idSku", idSku)
                .build();
    }

    public static Specification<SkuAtributoValor> byAtributo(Long idAtributo) {
        return SpecificationBuilder.<SkuAtributoValor>create()
                .activeOnly()
                .equal("atributo.idAtributo", idAtributo)
                .build();
    }

    public static Specification<SkuAtributoValor> byTipoDato(TipoDatoAtributo tipoDato) {
        return SpecificationBuilder.<SkuAtributoValor>create()
                .activeOnly()
                .equal("atributo.tipoDato", tipoDato)
                .build();
    }

    private static Boolean resolveEstado(SkuAtributoValorFilterDto filter) {
        if (filter == null) {
            return Boolean.TRUE;
        }

        if (Boolean.TRUE.equals(filter.incluirTodosLosEstados())) {
            return null;
        }

        return filter.estado() == null ? Boolean.TRUE : filter.estado();
    }
}