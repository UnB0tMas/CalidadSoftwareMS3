// ruta: src/main/java/com/upsjb/ms3/mapper/AtributoMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.dto.catalogo.atributo.request.AtributoCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.AtributoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.AtributoDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.AtributoResponseDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.TipoProductoAtributoResponseDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AtributoMapper {

    public Atributo toEntity(AtributoCreateRequestDto request) {
        if (request == null) {
            return null;
        }

        Atributo entity = new Atributo();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setTipoDato(request.tipoDato());
        entity.setUnidadMedida(request.unidadMedida());
        entity.setRequerido(defaultBoolean(request.requerido(), false));
        entity.setFiltrable(defaultBoolean(request.filtrable(), false));
        entity.setVisiblePublico(defaultBoolean(request.visiblePublico(), true));

        return entity;
    }

    public void updateEntity(Atributo entity, AtributoUpdateRequestDto request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setTipoDato(request.tipoDato());
        entity.setUnidadMedida(request.unidadMedida());
        entity.setRequerido(defaultBoolean(request.requerido(), false));
        entity.setFiltrable(defaultBoolean(request.filtrable(), false));
        entity.setVisiblePublico(defaultBoolean(request.visiblePublico(), true));
    }

    public AtributoResponseDto toResponse(Atributo entity) {
        if (entity == null) {
            return null;
        }

        return AtributoResponseDto.builder()
                .idAtributo(entity.getIdAtributo())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .tipoDato(entity.getTipoDato())
                .tipoDatoLabel(tipoDatoLabel(entity.getTipoDato()))
                .unidadMedida(entity.getUnidadMedida())
                .requerido(entity.getRequerido())
                .filtrable(entity.getFiltrable())
                .visiblePublico(entity.getVisiblePublico())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public AtributoDetailResponseDto toDetailResponse(
            Atributo entity,
            Long cantidadValoresProducto,
            Long cantidadValoresSku,
            List<TipoProductoAtributoResponseDto> tiposProductoAsociados
    ) {
        if (entity == null) {
            return null;
        }

        return AtributoDetailResponseDto.builder()
                .idAtributo(entity.getIdAtributo())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .tipoDato(entity.getTipoDato())
                .tipoDatoLabel(tipoDatoLabel(entity.getTipoDato()))
                .unidadMedida(entity.getUnidadMedida())
                .requerido(entity.getRequerido())
                .filtrable(entity.getFiltrable())
                .visiblePublico(entity.getVisiblePublico())
                .estado(entity.getEstado())
                .cantidadValoresProducto(defaultLong(cantidadValoresProducto))
                .cantidadValoresSku(defaultLong(cantidadValoresSku))
                .tiposProductoAsociados(tiposProductoAsociados == null ? List.of() : tiposProductoAsociados)
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

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}