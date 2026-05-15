// ruta: src/main/java/com/upsjb/ms3/mapper/ProductoAtributoValorMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoAtributoValor;
import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoAtributoValorRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoAtributoValorResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductoAtributoValorMapper {

    private final ReferenceMapper referenceMapper;

    public ProductoAtributoValor toEntity(
            ProductoAtributoValorRequestDto request,
            Producto producto,
            Atributo atributo
    ) {
        if (request == null) {
            return null;
        }

        ProductoAtributoValor entity = new ProductoAtributoValor();
        entity.setProducto(producto);
        entity.setAtributo(atributo);
        entity.setValorTexto(request.valorTexto());
        entity.setValorNumero(request.valorNumero());
        entity.setValorBoolean(request.valorBoolean());
        entity.setValorFecha(request.valorFecha());

        return entity;
    }

    public void updateEntity(
            ProductoAtributoValor entity,
            ProductoAtributoValorRequestDto request,
            Atributo atributo
    ) {
        if (entity == null || request == null) {
            return;
        }

        entity.setAtributo(atributo);
        entity.setValorTexto(request.valorTexto());
        entity.setValorNumero(request.valorNumero());
        entity.setValorBoolean(request.valorBoolean());
        entity.setValorFecha(request.valorFecha());
    }

    public ProductoAtributoValorResponseDto toResponse(ProductoAtributoValor entity) {
        if (entity == null) {
            return null;
        }

        Producto producto = entity.getProducto();
        Atributo atributo = entity.getAtributo();

        return ProductoAtributoValorResponseDto.builder()
                .idProductoAtributoValor(entity.getIdProductoAtributoValor())
                .idProducto(producto == null ? null : producto.getIdProducto())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .atributo(referenceMapper.toIdCodigoNombre(atributo))
                .tipoDato(atributo == null ? null : atributo.getTipoDato())
                .tipoDatoLabel(atributo == null ? null : tipoDatoLabel(atributo.getTipoDato()))
                .unidadMedida(atributo == null ? null : atributo.getUnidadMedida())
                .atributoRequerido(atributo == null ? null : atributo.getRequerido())
                .filtrable(atributo == null ? null : atributo.getFiltrable())
                .visiblePublico(atributo == null ? null : atributo.getVisiblePublico())
                .valorTexto(entity.getValorTexto())
                .valorNumero(entity.getValorNumero())
                .valorBoolean(entity.getValorBoolean())
                .valorFecha(entity.getValorFecha())
                .valorDisplay(valorDisplay(entity))
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String valorDisplay(ProductoAtributoValor entity) {
        if (entity == null || entity.getAtributo() == null || entity.getAtributo().getTipoDato() == null) {
            return null;
        }

        return switch (entity.getAtributo().getTipoDato()) {
            case TEXTO -> entity.getValorTexto();
            case NUMERO, DECIMAL -> entity.getValorNumero() == null ? null : entity.getValorNumero().toPlainString();
            case BOOLEANO -> entity.getValorBoolean() == null ? null : entity.getValorBoolean().toString();
            case FECHA -> entity.getValorFecha() == null ? null : entity.getValorFecha().toString();
        };
    }

    private String tipoDatoLabel(TipoDatoAtributo tipoDato) {
        return tipoDato == null ? null : tipoDato.getLabel();
    }
}