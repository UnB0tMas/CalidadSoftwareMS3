// ruta: src/main/java/com/upsjb/ms3/mapper/PromocionVersionMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Promocion;
import com.upsjb.ms3.domain.entity.PromocionVersion;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.dto.promocion.request.PromocionVersionCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionVersionEstadoRequestDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionVersionResponseDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PromocionVersionMapper {

    public PromocionVersion toEntity(
            PromocionVersionCreateRequestDto request,
            Promocion promocion,
            Long creadoPorIdUsuarioMs1
    ) {
        if (request == null) {
            return null;
        }

        EstadoPromocion estado = request.estadoPromocion() == null
                ? EstadoPromocion.BORRADOR
                : request.estadoPromocion();

        PromocionVersion entity = new PromocionVersion();
        entity.setPromocion(promocion);
        entity.setFechaInicio(request.fechaInicio());
        entity.setFechaFin(request.fechaFin());
        entity.setEstadoPromocion(estado);
        entity.setVisiblePublico(request.visiblePublico() == null ? Boolean.TRUE : request.visiblePublico());
        entity.setVigente(isLifecycleVisible(estado));
        entity.setMotivo(request.motivo());
        entity.setCreadoPorIdUsuarioMs1(creadoPorIdUsuarioMs1);

        return entity;
    }

    public void applyEstado(
            PromocionVersion entity,
            PromocionVersionEstadoRequestDto request
    ) {
        if (entity == null || request == null) {
            return;
        }

        entity.setEstadoPromocion(request.estadoPromocion());

        if (request.visiblePublico() != null) {
            entity.setVisiblePublico(request.visiblePublico());
        }

        entity.setMotivo(request.motivo());
    }

    public void closeVersion(PromocionVersion entity, String motivo) {
        if (entity == null) {
            return;
        }

        entity.setEstadoPromocion(EstadoPromocion.FINALIZADA);
        entity.setVigente(Boolean.FALSE);
        entity.setVisiblePublico(Boolean.FALSE);

        if (motivo != null) {
            entity.setMotivo(motivo);
        }
    }

    public PromocionVersionResponseDto toResponse(
            PromocionVersion entity,
            List<PromocionSkuDescuentoResponseDto> descuentos
    ) {
        if (entity == null) {
            return null;
        }

        Promocion promocion = entity.getPromocion();

        return PromocionVersionResponseDto.builder()
                .idPromocionVersion(entity.getIdPromocionVersion())
                .idPromocion(promocion == null ? null : promocion.getIdPromocion())
                .codigoPromocion(promocion == null ? null : promocion.getCodigo())
                .nombrePromocion(promocion == null ? null : promocion.getNombre())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .estadoPromocion(entity.getEstadoPromocion())
                .visiblePublico(entity.getVisiblePublico())
                .vigente(entity.getVigente())
                .motivo(entity.getMotivo())
                .creadoPorIdUsuarioMs1(entity.getCreadoPorIdUsuarioMs1())
                .estado(entity.getEstado())
                .descuentos(descuentos == null ? List.of() : descuentos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private boolean isLifecycleVisible(EstadoPromocion estado) {
        return estado == EstadoPromocion.ACTIVA || estado == EstadoPromocion.PROGRAMADA;
    }
}