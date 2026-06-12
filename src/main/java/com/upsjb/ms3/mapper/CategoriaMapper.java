package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.dto.catalogo.categoria.request.CategoriaCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.categoria.request.CategoriaUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.CategoriaAtributoResponseDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaResponseDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaTreeResponseDto;
import com.upsjb.ms3.dto.reference.response.CategoriaOptionDto;
import com.upsjb.ms3.dto.shared.IdCodigoNombreResponseDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public Categoria toEntity(
            CategoriaCreateRequestDto request,
            Categoria categoriaPadre,
            String codigo,
            String slug,
            Integer nivel
    ) {
        if (request == null) {
            return null;
        }

        Categoria entity =
                new Categoria();

        entity.setCategoriaPadre(
                categoriaPadre
        );

        entity.setCodigo(
                codigo
        );

        entity.setNombre(
                request.nombre()
        );

        entity.setSlug(slug);
        entity.setSlugGenerado(Boolean.TRUE);

        entity.setDescripcion(
                request.descripcion()
        );

        entity.setNivel(
                nivel == null
                        ? 1
                        : nivel
        );

        entity.setOrden(
                request.orden() == null
                        ? 0
                        : request.orden()
        );

        entity.setPermiteProductos(
                Boolean.TRUE.equals(
                        request.permiteProductos()
                )
        );

        return entity;
    }

    public void updateEntity(
            Categoria entity,
            CategoriaUpdateRequestDto request,
            Categoria categoriaPadre,
            Integer nivel
    ) {
        if (
                entity == null
                        || request == null
        ) {
            return;
        }

        entity.setCategoriaPadre(
                categoriaPadre
        );

        entity.setNombre(
                request.nombre()
        );

        entity.setDescripcion(
                request.descripcion()
        );

        entity.setNivel(
                nivel == null
                        ? 1
                        : nivel
        );

        entity.setOrden(
                request.orden() == null
                        ? 0
                        : request.orden()
        );

        entity.setPermiteProductos(
                Boolean.TRUE.equals(
                        request.permiteProductos()
                )
        );
    }

    public CategoriaResponseDto toResponse(
            Categoria entity
    ) {
        if (entity == null) {
            return null;
        }

        Categoria parent =
                entity.getCategoriaPadre();

        return CategoriaResponseDto.builder()
                .idCategoria(
                        entity.getIdCategoria()
                )
                .idCategoriaPadre(
                        parent == null
                                ? null
                                : parent.getIdCategoria()
                )
                .codigoCategoriaPadre(
                        parent == null
                                ? null
                                : parent.getCodigo()
                )
                .nombreCategoriaPadre(
                        parent == null
                                ? null
                                : parent.getNombre()
                )
                .codigo(
                        entity.getCodigo()
                )
                .nombre(
                        entity.getNombre()
                )
                .slug(
                        entity.getSlug()
                )
                .slugGenerado(
                        entity.getSlugGenerado()
                )
                .descripcion(
                        entity.getDescripcion()
                )
                .nivel(
                        entity.getNivel()
                )
                .orden(
                        entity.getOrden()
                )
                .permiteProductos(
                        entity.getPermiteProductos()
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


    public CategoriaDetailResponseDto toDetailResponse(
            Categoria entity,
            Long cantidadProductos,
            List<Categoria> subcategorias,
            List<CategoriaAtributoResponseDto> atributos
    ) {
        if (entity == null) {
            return null;
        }

        Categoria parent = entity.getCategoriaPadre();

        List<IdCodigoNombreResponseDto> children = subcategorias == null
                ? List.of()
                : subcategorias.stream()
                .filter(java.util.Objects::nonNull)
                .map(child -> IdCodigoNombreResponseDto.builder()
                        .id(child.getIdCategoria())
                        .codigo(child.getCodigo())
                        .nombre(child.getNombre())
                        .build())
                .toList();

        return CategoriaDetailResponseDto.builder()
                .idCategoria(entity.getIdCategoria())
                .categoriaPadre(parent == null
                        ? null
                        : IdCodigoNombreResponseDto.builder()
                        .id(parent.getIdCategoria())
                        .codigo(parent.getCodigo())
                        .nombre(parent.getNombre())
                        .build())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .slugGenerado(entity.getSlugGenerado())
                .descripcion(entity.getDescripcion())
                .nivel(entity.getNivel())
                .orden(entity.getOrden())
                .permiteProductos(entity.getPermiteProductos())
                .estado(entity.getEstado())
                .cantidadProductos(cantidadProductos == null ? 0L : cantidadProductos)
                .subcategorias(children)
                .atributos(atributos == null ? List.of() : atributos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CategoriaTreeResponseDto toTreeResponse(
            Categoria entity,
            List<CategoriaTreeResponseDto> children
    ) {
        if (entity == null) {
            return null;
        }

        return CategoriaTreeResponseDto.builder()
                .idCategoria(
                        entity.getIdCategoria()
                )
                .codigo(
                        entity.getCodigo()
                )
                .nombre(
                        entity.getNombre()
                )
                .slug(
                        entity.getSlug()
                )
                .nivel(
                        entity.getNivel()
                )
                .orden(
                        entity.getOrden()
                )
                .permiteProductos(
                        entity.getPermiteProductos()
                )
                .estado(
                        entity.getEstado()
                )
                .hijos(
                        children == null
                                ? List.of()
                                : children
                )
                .build();
    }

    public CategoriaOptionDto toOption(
            Categoria entity
    ) {
        if (entity == null) {
            return null;
        }

        return CategoriaOptionDto.builder()
                .idCategoria(
                        entity.getIdCategoria()
                )
                .idCategoriaPadre(
                        entity.getCategoriaPadre() == null
                                ? null
                                : entity.getCategoriaPadre()
                                .getIdCategoria()
                )
                .codigo(
                        entity.getCodigo()
                )
                .nombre(
                        entity.getNombre()
                )
                .slug(
                        entity.getSlug()
                )
                .nivel(
                        entity.getNivel()
                )
                .orden(
                        entity.getOrden()
                )
                .permiteProductos(
                        entity.getPermiteProductos()
                )
                .estado(
                        entity.getEstado()
                )
                .build();
    }
}
