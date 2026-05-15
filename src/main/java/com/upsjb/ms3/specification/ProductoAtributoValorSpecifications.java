// ruta: src/main/java/com/upsjb/ms3/specification/ProductoAtributoValorSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.ProductoAtributoValor;
import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoAtributoValorFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;

public final class ProductoAtributoValorSpecifications {

    private ProductoAtributoValorSpecifications() {
    }

    public static Specification<ProductoAtributoValor> fromFilter(ProductoAtributoValorFilterDto filter) {
        if (filter == null) {
            return activeOnly();
        }

        return SpecificationBuilder.<ProductoAtributoValor>create()
                .textSearch(
                        filter.search(),
                        "producto.codigoProducto",
                        "producto.nombre",
                        "producto.slug",
                        "atributo.codigo",
                        "atributo.nombre",
                        "valorTexto"
                )
                .equal("producto.idProducto", filter.idProducto())
                .like("producto.codigoProducto", filter.codigoProducto())
                .like("producto.nombre", filter.nombreProducto())
                .like("producto.slug", filter.slugProducto())
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

    public static Specification<ProductoAtributoValor> activeOnly() {
        return SpecificationBuilder.<ProductoAtributoValor>create()
                .activeOnly()
                .build();
    }

    public static Specification<ProductoAtributoValor> byProducto(Long idProducto) {
        return SpecificationBuilder.<ProductoAtributoValor>create()
                .activeOnly()
                .equal("producto.idProducto", idProducto)
                .build();
    }

    public static Specification<ProductoAtributoValor> byAtributo(Long idAtributo) {
        return SpecificationBuilder.<ProductoAtributoValor>create()
                .activeOnly()
                .equal("atributo.idAtributo", idAtributo)
                .build();
    }

    public static Specification<ProductoAtributoValor> byTipoDato(TipoDatoAtributo tipoDato) {
        return SpecificationBuilder.<ProductoAtributoValor>create()
                .activeOnly()
                .equal("atributo.tipoDato", tipoDato)
                .build();
    }

    private static Boolean resolveEstado(ProductoAtributoValorFilterDto filter) {
        if (filter == null) {
            return Boolean.TRUE;
        }

        if (Boolean.TRUE.equals(filter.incluirTodosLosEstados())) {
            return null;
        }

        return filter.estado() == null ? Boolean.TRUE : filter.estado();
    }
}