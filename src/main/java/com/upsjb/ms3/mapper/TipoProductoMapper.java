// ruta: src/main/java/com/upsjb/ms3/mapper/TipoProductoMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.request.TipoProductoCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.request.TipoProductoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.response.TipoProductoDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.response.TipoProductoResponseDto;
import org.springframework.stereotype.Component;

@Component
public class TipoProductoMapper {

    public TipoProducto toEntity(TipoProductoCreateRequestDto request) {
        if (request == null) {
            return null;
        }

        TipoProducto entity = new TipoProducto();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setDescripcion(request.descripcion());

        return entity;
    }

    public void updateEntity(TipoProducto entity, TipoProductoUpdateRequestDto request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setDescripcion(request.descripcion());
    }

    public TipoProductoResponseDto toResponse(TipoProducto entity) {
        if (entity == null) {
            return null;
        }

        return TipoProductoResponseDto.builder()
                .idTipoProducto(entity.getIdTipoProducto())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TipoProductoDetailResponseDto toDetailResponse(
            TipoProducto entity,
            Long cantidadAtributos,
            Long cantidadProductos
    ) {
        if (entity == null) {
            return null;
        }

        return TipoProductoDetailResponseDto.builder()
                .idTipoProducto(entity.getIdTipoProducto())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .estado(entity.getEstado())
                .cantidadAtributos(defaultLong(cantidadAtributos))
                .cantidadProductos(defaultLong(cantidadProductos))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}