// ruta: src/main/java/com/upsjb/ms3/mapper/SkuAtributoValorMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.SkuAtributoValor;
import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.dto.catalogo.producto.request.SkuAtributoValorRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.SkuAtributoValorResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SkuAtributoValorMapper {

    private final ReferenceMapper referenceMapper;

    public SkuAtributoValor toEntity(
            SkuAtributoValorRequestDto request,
            ProductoSku sku,
            Atributo atributo
    ) {
        if (request == null) {
            return null;
        }

        SkuAtributoValor entity = new SkuAtributoValor();
        entity.setSku(sku);
        entity.setAtributo(atributo);
        entity.setValorTexto(request.valorTexto());
        entity.setValorNumero(request.valorNumero());
        entity.setValorBoolean(request.valorBoolean());
        entity.setValorFecha(request.valorFecha());

        return entity;
    }

    public void updateEntity(
            SkuAtributoValor entity,
            SkuAtributoValorRequestDto request,
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

    public SkuAtributoValorResponseDto toResponse(SkuAtributoValor entity) {
        if (entity == null) {
            return null;
        }

        ProductoSku sku = entity.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        Atributo atributo = entity.getAtributo();

        return SkuAtributoValorResponseDto.builder()
                .idSkuAtributoValor(entity.getIdSkuAtributoValor())
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .idProducto(producto == null ? null : producto.getIdProducto())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .atributo(referenceMapper.toIdCodigoNombre(atributo))
                .tipoDato(atributo == null ? null : atributo.getTipoDato())
                .tipoDatoLabel(atributo == null ? null : tipoDatoLabel(atributo.getTipoDato()))
                .unidadMedida(atributo == null ? null : atributo.getUnidadMedida())
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

    private String valorDisplay(SkuAtributoValor entity) {
        if (entity == null || entity.getAtributo() == null || entity.getAtributo().getTipoDato() == null) {
            return null;
        }

        return switch (entity.getAtributo().getTipoDato()) {
            case TEXTO -> entity.getValorTexto();
            case NUMERO -> entity.getValorNumero() == null ? null : entity.getValorNumero().toPlainString();
            case BOOLEAN -> entity.getValorBoolean() == null ? null : entity.getValorBoolean().toString();
            case FECHA -> entity.getValorFecha() == null ? null : entity.getValorFecha().toString();
        };
    }

    private String tipoDatoLabel(TipoDatoAtributo tipoDato) {
        return tipoDato == null ? null : tipoDato.getLabel();
    }
}