// ruta: src/main/java/com/upsjb/ms3/mapper/CategoriaMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.dto.catalogo.categoria.request.CategoriaCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.categoria.request.CategoriaUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaResponseDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaTreeResponseDto;
import com.upsjb.ms3.dto.shared.IdCodigoNombreResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoriaMapper {

    private final ReferenceMapper referenceMapper;

    public Categoria toEntity(
            CategoriaCreateRequestDto request,
            Categoria categoriaPadre,
            String slug,
            Boolean slugGenerado
    ) {
        if (request == null) {
            return null;
        }

        Categoria entity = new Categoria();
        entity.setCategoriaPadre(categoriaPadre);
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setSlug(slug);
        entity.setSlugGenerado(slugGenerado == null || slugGenerado);
        entity.setDescripcion(request.descripcion());
        entity.setNivel(resolveNivel(categoriaPadre));
        entity.setOrden(defaultInteger(request.orden(), 0));

        return entity;
    }

    public void updateEntity(
            Categoria entity,
            CategoriaUpdateRequestDto request,
            Categoria categoriaPadre,
            String slug,
            Boolean slugGenerado
    ) {
        if (entity == null || request == null) {
            return;
        }

        entity.setCategoriaPadre(categoriaPadre);
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());

        if (slug != null) {
            entity.setSlug(slug);
        }

        if (slugGenerado != null) {
            entity.setSlugGenerado(slugGenerado);
        }

        entity.setDescripcion(request.descripcion());
        entity.setNivel(resolveNivel(categoriaPadre));
        entity.setOrden(defaultInteger(request.orden(), 0));
    }

    public CategoriaResponseDto toResponse(Categoria entity) {
        if (entity == null) {
            return null;
        }

        Categoria padre = entity.getCategoriaPadre();

        return CategoriaResponseDto.builder()
                .idCategoria(entity.getIdCategoria())
                .idCategoriaPadre(padre == null ? null : padre.getIdCategoria())
                .codigoCategoriaPadre(padre == null ? null : padre.getCodigo())
                .nombreCategoriaPadre(padre == null ? null : padre.getNombre())
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
            List<IdCodigoNombreResponseDto> subcategorias
    ) {
        if (entity == null) {
            return null;
        }

        return CategoriaDetailResponseDto.builder()
                .idCategoria(entity.getIdCategoria())
                .categoriaPadre(referenceMapper.toIdCodigoNombre(entity.getCategoriaPadre()))
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .slugGenerado(entity.getSlugGenerado())
                .descripcion(entity.getDescripcion())
                .nivel(entity.getNivel())
                .orden(entity.getOrden())
                .estado(entity.getEstado())
                .cantidadProductos(defaultLong(cantidadProductos))
                .subcategorias(subcategorias == null ? List.of() : subcategorias)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CategoriaTreeResponseDto toTreeResponse(
            Categoria entity,
            List<CategoriaTreeResponseDto> hijos
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
                .hijos(hijos == null ? List.of() : hijos)
                .build();
    }

    public IdCodigoNombreResponseDto toSummary(Categoria entity) {
        return referenceMapper.toIdCodigoNombre(entity);
    }

    private Integer resolveNivel(Categoria categoriaPadre) {
        if (categoriaPadre == null || categoriaPadre.getNivel() == null) {
            return 1;
        }

        return categoriaPadre.getNivel() + 1;
    }

    private Integer defaultInteger(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}