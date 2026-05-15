// ruta: src/main/java/com/upsjb/ms3/mapper/EmpleadoInventarioPermisoMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.EmpleadoInventarioPermisoHistorial;
import com.upsjb.ms3.domain.entity.EmpleadoSnapshotMs2;
import com.upsjb.ms3.dto.empleado.request.EmpleadoInventarioPermisoRevokeRequestDto;
import com.upsjb.ms3.dto.empleado.request.EmpleadoInventarioPermisoUpdateRequestDto;
import com.upsjb.ms3.dto.empleado.response.EmpleadoInventarioPermisoResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmpleadoInventarioPermisoMapper {

    private final EmpleadoSnapshotMs2Mapper empleadoSnapshotMs2Mapper;

    public EmpleadoInventarioPermisoHistorial toEntity(
            EmpleadoInventarioPermisoUpdateRequestDto request,
            EmpleadoSnapshotMs2 empleadoSnapshot,
            Long otorgadoPorIdUsuarioMs1
    ) {
        if (request == null) {
            return null;
        }

        EmpleadoInventarioPermisoHistorial entity = new EmpleadoInventarioPermisoHistorial();
        entity.setEmpleadoSnapshot(empleadoSnapshot);
        entity.setPuedeCrearProductoBasico(defaultBoolean(request.puedeCrearProductoBasico(), false));
        entity.setPuedeEditarProductoBasico(defaultBoolean(request.puedeEditarProductoBasico(), false));
        entity.setPuedeRegistrarEntrada(defaultBoolean(request.puedeRegistrarEntrada(), false));
        entity.setPuedeRegistrarSalida(defaultBoolean(request.puedeRegistrarSalida(), false));
        entity.setPuedeRegistrarAjuste(defaultBoolean(request.puedeRegistrarAjuste(), false));
        entity.setPuedeConsultarKardex(defaultBoolean(request.puedeConsultarKardex(), false));
        entity.setPuedeGestionarImagenes(defaultBoolean(request.puedeGestionarImagenes(), false));
        entity.setPuedeActualizarAtributos(defaultBoolean(request.puedeActualizarAtributos(), false));
        entity.setFechaInicio(defaultDateTime(request.fechaInicio()));
        entity.setFechaFin(request.fechaFin());
        entity.setVigente(resolveVigente(request.fechaFin()));
        entity.setOtorgadoPorIdUsuarioMs1(otorgadoPorIdUsuarioMs1);
        entity.setMotivo(request.motivo());

        return entity;
    }

    public void closeVigencia(
            EmpleadoInventarioPermisoHistorial entity,
            Long revocadoPorIdUsuarioMs1,
            LocalDateTime fechaFin,
            String motivo
    ) {
        if (entity == null) {
            return;
        }

        entity.setFechaFin(defaultDateTime(fechaFin));
        entity.setVigente(Boolean.FALSE);
        entity.setRevocadoPorIdUsuarioMs1(revocadoPorIdUsuarioMs1);

        if (motivo != null && !motivo.isBlank()) {
            entity.setMotivo(motivo);
        }
    }

    public void closeVigencia(
            EmpleadoInventarioPermisoHistorial entity,
            Long revocadoPorIdUsuarioMs1,
            EmpleadoInventarioPermisoRevokeRequestDto request
    ) {
        if (request == null) {
            closeVigencia(entity, revocadoPorIdUsuarioMs1, LocalDateTime.now(), null);
            return;
        }

        closeVigencia(entity, revocadoPorIdUsuarioMs1, request.fechaFin(), request.motivo());
    }

    public EmpleadoInventarioPermisoResponseDto toResponse(EmpleadoInventarioPermisoHistorial entity) {
        if (entity == null) {
            return null;
        }

        return EmpleadoInventarioPermisoResponseDto.builder()
                .idPermisoHistorial(entity.getIdPermisoHistorial())
                .empleado(empleadoSnapshotMs2Mapper.toResponse(entity.getEmpleadoSnapshot()))
                .puedeCrearProductoBasico(entity.getPuedeCrearProductoBasico())
                .puedeEditarProductoBasico(entity.getPuedeEditarProductoBasico())
                .puedeRegistrarEntrada(entity.getPuedeRegistrarEntrada())
                .puedeRegistrarSalida(entity.getPuedeRegistrarSalida())
                .puedeRegistrarAjuste(entity.getPuedeRegistrarAjuste())
                .puedeConsultarKardex(entity.getPuedeConsultarKardex())
                .puedeGestionarImagenes(entity.getPuedeGestionarImagenes())
                .puedeActualizarAtributos(entity.getPuedeActualizarAtributos())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .vigente(entity.getVigente())
                .otorgadoPorIdUsuarioMs1(entity.getOtorgadoPorIdUsuarioMs1())
                .revocadoPorIdUsuarioMs1(entity.getRevocadoPorIdUsuarioMs1())
                .motivo(entity.getMotivo())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<EmpleadoInventarioPermisoResponseDto> toResponseList(
            List<EmpleadoInventarioPermisoHistorial> entities
    ) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(this::toResponse)
                .toList();
    }

    public Boolean hasAnyPermission(EmpleadoInventarioPermisoHistorial entity) {
        if (entity == null) {
            return false;
        }

        return Boolean.TRUE.equals(entity.getPuedeCrearProductoBasico())
                || Boolean.TRUE.equals(entity.getPuedeEditarProductoBasico())
                || Boolean.TRUE.equals(entity.getPuedeRegistrarEntrada())
                || Boolean.TRUE.equals(entity.getPuedeRegistrarSalida())
                || Boolean.TRUE.equals(entity.getPuedeRegistrarAjuste())
                || Boolean.TRUE.equals(entity.getPuedeConsultarKardex())
                || Boolean.TRUE.equals(entity.getPuedeGestionarImagenes())
                || Boolean.TRUE.equals(entity.getPuedeActualizarAtributos());
    }

    private Boolean resolveVigente(LocalDateTime fechaFin) {
        return fechaFin == null || fechaFin.isAfter(LocalDateTime.now());
    }

    private Boolean defaultBoolean(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    private LocalDateTime defaultDateTime(LocalDateTime value) {
        return value == null ? LocalDateTime.now() : value;
    }
}