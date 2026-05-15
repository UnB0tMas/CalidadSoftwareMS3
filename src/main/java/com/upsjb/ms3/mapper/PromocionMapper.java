// ruta: src/main/java/com/upsjb/ms3/mapper/PromocionMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Promocion;
import com.upsjb.ms3.dto.promocion.request.PromocionCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionUpdateRequestDto;
import com.upsjb.ms3.dto.promocion.response.PromocionResponseDto;
import org.springframework.stereotype.Component;

@Component
public class PromocionMapper {

    public Promocion toEntity(
            PromocionCreateRequestDto request,
            String codigo,
            Long creadoPorIdUsuarioMs1
    ) {
        if (request == null) {
            return null;
        }

        Promocion entity = new Promocion();
        entity.setCodigo(codigo);
        entity.setCodigoGenerado(Boolean.TRUE);
        entity.setNombre(request.nombre());
        entity.setDescripcion(request.descripcion());
        entity.setCreadoPorIdUsuarioMs1(creadoPorIdUsuarioMs1);

        return entity;
    }

    public void updateEntity(Promocion entity, PromocionUpdateRequestDto request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setNombre(request.nombre());
        entity.setDescripcion(request.descripcion());
    }

    public PromocionResponseDto toResponse(Promocion entity) {
        if (entity == null) {
            return null;
        }

        return PromocionResponseDto.builder()
                .idPromocion(entity.getIdPromocion())
                .codigo(entity.getCodigo())
                .codigoGenerado(entity.getCodigoGenerado())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .creadoPorIdUsuarioMs1(entity.getCreadoPorIdUsuarioMs1())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}