// ruta: src/main/java/com/upsjb/ms3/mapper/MarcaMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.dto.catalogo.marca.request.MarcaCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.marca.request.MarcaUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.marca.response.MarcaDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.marca.response.MarcaResponseDto;
import org.springframework.stereotype.Component;

@Component
public class MarcaMapper {

    public Marca toEntity(
            MarcaCreateRequestDto request,
            String slug,
            Boolean slugGenerado
    ) {
        if (request == null) {
            return null;
        }

        Marca entity = new Marca();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setSlug(slug);
        entity.setSlugGenerado(slugGenerado == null || slugGenerado);
        entity.setDescripcion(request.descripcion());

        return entity;
    }

    public void updateEntity(
            Marca entity,
            MarcaUpdateRequestDto request,
            String slug,
            Boolean slugGenerado
    ) {
        if (entity == null || request == null) {
            return;
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());

        if (slug != null) {
            entity.setSlug(slug);
        }

        if (slugGenerado != null) {
            entity.setSlugGenerado(slugGenerado);
        }

        entity.setDescripcion(request.descripcion());
    }

    public MarcaResponseDto toResponse(Marca entity) {
        if (entity == null) {
            return null;
        }

        return MarcaResponseDto.builder()
                .idMarca(entity.getIdMarca())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .slugGenerado(entity.getSlugGenerado())
                .descripcion(entity.getDescripcion())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public MarcaDetailResponseDto toDetailResponse(
            Marca entity,
            Long cantidadProductos
    ) {
        if (entity == null) {
            return null;
        }

        return MarcaDetailResponseDto.builder()
                .idMarca(entity.getIdMarca())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .slugGenerado(entity.getSlugGenerado())
                .descripcion(entity.getDescripcion())
                .estado(entity.getEstado())
                .cantidadProductos(defaultLong(cantidadProductos))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}