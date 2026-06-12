package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.CategoriaAtributo;
import com.upsjb.ms3.dto.catalogo.atributo.request.CategoriaAtributoAssignRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.CategoriaAtributoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoriaAtributoMapper {

    private final ReferenceMapper referenceMapper;

    public CategoriaAtributo toEntity(
            CategoriaAtributoAssignRequestDto request,
            Categoria categoria,
            Atributo atributo
    ) {
        if (request == null) {
            return null;
        }

        CategoriaAtributo entity =
                new CategoriaAtributo();

        entity.setCategoria(categoria);
        entity.setAtributo(atributo);

        entity.setRequerido(
                Boolean.TRUE.equals(
                        request.requerido()
                )
        );

        entity.setOrden(
                request.orden() == null
                        ? 0
                        : request.orden()
        );

        return entity;
    }

    public void updateEntity(
            CategoriaAtributo entity,
            Boolean requerido,
            Integer orden
    ) {
        if (entity == null) {
            return;
        }

        if (requerido != null) {
            entity.setRequerido(
                    requerido
            );
        }

        if (orden != null) {
            entity.setOrden(
                    orden
            );
        }
    }

    public CategoriaAtributoResponseDto toResponse(
            CategoriaAtributo entity
    ) {
        if (entity == null) {
            return null;
        }

        Atributo atributo =
                entity.getAtributo();

        return CategoriaAtributoResponseDto.builder()
                .idCategoriaAtributo(
                        entity.getIdCategoriaAtributo()
                )
                .categoria(
                        referenceMapper.toIdCodigoNombre(
                                entity.getCategoria()
                        )
                )
                .atributo(
                        referenceMapper.toIdCodigoNombre(
                                atributo
                        )
                )
                .tipoDato(
                        atributo == null
                                ? null
                                : atributo.getTipoDato()
                )
                .tipoDatoLabel(
                        atributo == null
                                || atributo.getTipoDato() == null
                                ? null
                                : atributo.getTipoDato()
                                .getLabel()
                )
                .unidadMedida(
                        atributo == null
                                ? null
                                : atributo.getUnidadMedida()
                )
                .atributoRequeridoBase(
                        atributo == null
                                ? null
                                : atributo.getRequerido()
                )
                .filtrable(
                        atributo == null
                                ? null
                                : atributo.getFiltrable()
                )
                .visiblePublico(
                        atributo == null
                                ? null
                                : atributo.getVisiblePublico()
                )
                .requerido(
                        entity.getRequerido()
                )
                .orden(
                        entity.getOrden()
                )
                .estado(
                        entity.getEstado()
                )
                .createdAt(
                        entity.getCreatedAt()
                )
                .updatedAt(
                        entity.getUpdatedAt()
                )
                .build();
    }
}