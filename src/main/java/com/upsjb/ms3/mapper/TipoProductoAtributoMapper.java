// ruta: src/main/java/com/upsjb/ms3/mapper/TipoProductoAtributoMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.domain.entity.TipoProductoAtributo;
import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.dto.catalogo.atributo.request.TipoProductoAtributoAssignRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.TipoProductoAtributoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TipoProductoAtributoMapper {

    private final ReferenceMapper referenceMapper;

    public TipoProductoAtributo toEntity(
            TipoProductoAtributoAssignRequestDto request,
            TipoProducto tipoProducto,
            Atributo atributo
    ) {
        if (request == null) {
            return null;
        }

        TipoProductoAtributo entity = new TipoProductoAtributo();
        entity.setTipoProducto(tipoProducto);
        entity.setAtributo(atributo);
        entity.setRequerido(defaultBoolean(request.requerido(), false));
        entity.setOrden(defaultInteger(request.orden(), 0));

        return entity;
    }

    public void updateEntity(
            TipoProductoAtributo entity,
            Boolean requerido,
            Integer orden
    ) {
        if (entity == null) {
            return;
        }

        if (requerido != null) {
            entity.setRequerido(requerido);
        }

        if (orden != null) {
            entity.setOrden(orden);
        }
    }

    public TipoProductoAtributoResponseDto toResponse(TipoProductoAtributo entity) {
        if (entity == null) {
            return null;
        }

        Atributo atributo = entity.getAtributo();

        return TipoProductoAtributoResponseDto.builder()
                .idTipoProductoAtributo(entity.getIdTipoProductoAtributo())
                .tipoProducto(referenceMapper.toIdCodigoNombre(entity.getTipoProducto()))
                .atributo(referenceMapper.toIdCodigoNombre(atributo))
                .tipoDato(atributo == null ? null : atributo.getTipoDato())
                .tipoDatoLabel(atributo == null ? null : tipoDatoLabel(atributo.getTipoDato()))
                .unidadMedida(atributo == null ? null : atributo.getUnidadMedida())
                .atributoRequeridoBase(atributo == null ? null : atributo.getRequerido())
                .filtrable(atributo == null ? null : atributo.getFiltrable())
                .visiblePublico(atributo == null ? null : atributo.getVisiblePublico())
                .requerido(entity.getRequerido())
                .orden(entity.getOrden())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String tipoDatoLabel(TipoDatoAtributo tipoDato) {
        return tipoDato == null ? null : tipoDato.getLabel();
    }

    private Boolean defaultBoolean(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Integer defaultInteger(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }
}