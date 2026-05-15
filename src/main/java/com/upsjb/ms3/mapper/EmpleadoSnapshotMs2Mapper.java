// ruta: src/main/java/com/upsjb/ms3/mapper/EmpleadoSnapshotMs2Mapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.EmpleadoSnapshotMs2;
import com.upsjb.ms3.dto.empleado.request.EmpleadoSnapshotMs2UpsertRequestDto;
import com.upsjb.ms3.dto.empleado.response.EmpleadoSnapshotMs2ResponseDto;
import com.upsjb.ms3.integration.ms2.Ms2EmpleadoSnapshotClient;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EmpleadoSnapshotMs2Mapper {

    public EmpleadoSnapshotMs2 toEntity(EmpleadoSnapshotMs2UpsertRequestDto request) {
        if (request == null) {
            return null;
        }

        EmpleadoSnapshotMs2 entity = new EmpleadoSnapshotMs2();
        applyUpsert(entity, request);
        return entity;
    }

    public EmpleadoSnapshotMs2 toEntity(Ms2EmpleadoSnapshotClient.EmpleadoSnapshotResponse response) {
        if (response == null) {
            return null;
        }

        EmpleadoSnapshotMs2 entity = new EmpleadoSnapshotMs2();
        applyMs2Response(entity, response);
        return entity;
    }

    public void updateEntity(EmpleadoSnapshotMs2 entity, EmpleadoSnapshotMs2UpsertRequestDto request) {
        if (entity == null || request == null) {
            return;
        }

        applyUpsert(entity, request);
    }

    public void updateEntity(EmpleadoSnapshotMs2 entity, Ms2EmpleadoSnapshotClient.EmpleadoSnapshotResponse response) {
        if (entity == null || response == null) {
            return;
        }

        applyMs2Response(entity, response);
    }

    public Ms2EmpleadoSnapshotClient.EmpleadoSnapshotRequest toMs2Request(EmpleadoSnapshotMs2UpsertRequestDto request) {
        if (request == null) {
            return null;
        }

        return new Ms2EmpleadoSnapshotClient.EmpleadoSnapshotRequest(
                request.idEmpleadoMs2(),
                request.idUsuarioMs1(),
                request.codigoEmpleado(),
                request.nombresCompletos(),
                request.areaCodigo(),
                request.areaNombre(),
                request.empleadoActivo(),
                request.snapshotVersion(),
                request.snapshotAt()
        );
    }

    public EmpleadoSnapshotMs2ResponseDto toResponse(EmpleadoSnapshotMs2 entity) {
        if (entity == null) {
            return null;
        }

        return EmpleadoSnapshotMs2ResponseDto.builder()
                .idEmpleadoSnapshot(entity.getIdEmpleadoSnapshot())
                .idEmpleadoMs2(entity.getIdEmpleadoMs2())
                .idUsuarioMs1(entity.getIdUsuarioMs1())
                .codigoEmpleado(entity.getCodigoEmpleado())
                .nombresCompletos(entity.getNombresCompletos())
                .areaCodigo(entity.getAreaCodigo())
                .areaNombre(entity.getAreaNombre())
                .empleadoActivo(entity.getEmpleadoActivo())
                .snapshotVersion(entity.getSnapshotVersion())
                .snapshotAt(entity.getSnapshotAt())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<EmpleadoSnapshotMs2ResponseDto> toResponseList(List<EmpleadoSnapshotMs2> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(this::toResponse)
                .toList();
    }

    private void applyUpsert(EmpleadoSnapshotMs2 entity, EmpleadoSnapshotMs2UpsertRequestDto request) {
        entity.setIdEmpleadoMs2(request.idEmpleadoMs2());
        entity.setIdUsuarioMs1(request.idUsuarioMs1());
        entity.setCodigoEmpleado(request.codigoEmpleado());
        entity.setNombresCompletos(request.nombresCompletos());
        entity.setAreaCodigo(request.areaCodigo());
        entity.setAreaNombre(request.areaNombre());
        entity.setEmpleadoActivo(defaultBoolean(request.empleadoActivo(), true));
        entity.setSnapshotVersion(request.snapshotVersion());
        entity.setSnapshotAt(defaultDateTime(request.snapshotAt()));

        if (request.estado() != null) {
            entity.setEstado(request.estado());
        }
    }

    private void applyMs2Response(
            EmpleadoSnapshotMs2 entity,
            Ms2EmpleadoSnapshotClient.EmpleadoSnapshotResponse response
    ) {
        entity.setIdEmpleadoMs2(response.idEmpleadoMs2());
        entity.setIdUsuarioMs1(response.idUsuarioMs1());
        entity.setCodigoEmpleado(response.codigoEmpleado());
        entity.setNombresCompletos(response.nombresCompletos());
        entity.setAreaCodigo(response.areaCodigo());
        entity.setAreaNombre(response.areaNombre());
        entity.setEmpleadoActivo(defaultBoolean(response.empleadoActivo(), true));
        entity.setSnapshotVersion(response.snapshotVersion());
        entity.setSnapshotAt(defaultDateTime(response.snapshotAt()));
    }

    private Boolean defaultBoolean(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    private LocalDateTime defaultDateTime(LocalDateTime value) {
        return value == null ? LocalDateTime.now() : value;
    }
}