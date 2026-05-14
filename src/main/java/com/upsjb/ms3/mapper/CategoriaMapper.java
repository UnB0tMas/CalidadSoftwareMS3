// ruta: src/main/java/com/upsjb/ms3/mapper/CategoriaMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.dto.catalogo.categoria.request.CategoriaCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.categoria.request.CategoriaUpdateRequestDto;
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
            String slug,
            Integer nivel
    ) {
        if (request == null) {
            return null;
        }

        Categoria entity = new Categoria();
        entity.setCategoriaPadre(categoriaPadre);
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setSlug(slug);
        entity.setSlugGenerado(Boolean.TRUE);
        entity.setDescripcion(request.descripcion());
        entity.setNivel(nivel);
        entity.setOrden(request.orden() == null ? 0 : request.orden());

        return entity;
    }

    public void updateEntity(
            Categoria entity,
            CategoriaUpdateRequestDto request,
            Categoria categoriaPadre,
            String slug,
            Integer nivel
    ) {
        if (entity == null || request == null) {
            return;
        }

        entity.setCategoriaPadre(categoriaPadre);
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setSlug(slug);
        entity.setSlugGenerado(Boolean.TRUE);
        entity.setDescripcion(request.descripcion());
        entity.setNivel(nivel);
        entity.setOrden(request.orden() == null ? 0 : request.orden());
    }

    public CategoriaResponseDto toResponse(Categoria entity) {
        if (entity == null) {
            return null;
        }

        Categoria parent = entity.getCategoriaPadre();

        return CategoriaResponseDto.builder()
                .idCategoria(entity.getIdCategoria())
                .idCategoriaPadre(parent == null ? null : parent.getIdCategoria())
                .codigoCategoriaPadre(parent == null ? null : parent.getCodigo())
                .nombreCategoriaPadre(parent == null ? null : parent.getNombre())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .slugGenerado(entity.getSlugGenerado())
                .descripcion(entity.getDescripcion())
                .nivel(entity.getNivel())
                .orden(entity.getOrden())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CategoriaDetailResponseDto toDetailResponse(
            Categoria entity,
            Long cantidadProductos,
            List<Categoria> subcategorias
    ) {
        if (entity == null) {
            return null;
        }

        return CategoriaDetailResponseDto.builder()
                .idCategoria(entity.getIdCategoria())
                .categoriaPadre(toIdCodigoNombre(entity.getCategoriaPadre()))
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .slugGenerado(entity.getSlugGenerado())
                .descripcion(entity.getDescripcion())
                .nivel(entity.getNivel())
                .orden(entity.getOrden())
                .estado(entity.getEstado())
                .cantidadProductos(cantidadProductos == null ? 0L : cantidadProductos)
                .subcategorias(subcategorias == null
                        ? List.of()
                        : subcategorias.stream().map(this::toIdCodigoNombre).toList())
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
                .idCategoria(entity.getIdCategoria())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .nivel(entity.getNivel())
                .orden(entity.getOrden())
                .estado(entity.getEstado())
                .hijos(children == null ? List.of() : children)
                .build();
    }

    public CategoriaOptionDto toOption(Categoria entity) {
        if (entity == null) {
            return null;
        }

        return CategoriaOptionDto.builder()
                .idCategoria(entity.getIdCategoria())
                .idCategoriaPadre(entity.getCategoriaPadre() == null ? null : entity.getCategoriaPadre().getIdCategoria())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .nivel(entity.getNivel())
                .orden(entity.getOrden())
                .estado(entity.getEstado())
                .build();
    }

    private IdCodigoNombreResponseDto toIdCodigoNombre(Categoria entity) {
        if (entity == null) {
            return null;
        }

        return IdCodigoNombreResponseDto.builder()
                .id(entity.getIdCategoria())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .estado(entity.getEstado())
                .build();
    }
}