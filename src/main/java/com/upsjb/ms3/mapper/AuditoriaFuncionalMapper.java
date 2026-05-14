// ruta: src/main/java/com/upsjb/ms3/mapper/AuditoriaFuncionalMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.AuditoriaFuncional;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.ResultadoAuditoria;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.auditoria.response.AuditoriaFuncionalResponseDto;
import com.upsjb.ms3.shared.audit.AuditContext;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AuditoriaFuncionalMapper {

    public AuditoriaFuncional toEntity(
            AuditContext context,
            TipoEventoAuditoria tipoEvento,
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            ResultadoAuditoria resultado,
            String descripcion,
            String metadataJson
    ) {
        AuditoriaFuncional entity = new AuditoriaFuncional();
        AuditContext safeContext = context == null ? AuditContext.empty() : context;

        entity.setIdUsuarioActorMs1(safeContext.idUsuarioActorMs1());
        entity.setIdEmpleadoActorMs2(safeContext.idEmpleadoActorMs2());
        entity.setRolActor(resolveRol(safeContext));
        entity.setTipoEvento(tipoEvento);
        entity.setEntidad(entidad);
        entity.setIdRegistroAfectado(clean(idRegistroAfectado, 100));
        entity.setAccion(cleanRequired(accion, 120));
        entity.setResultado(resultado == null ? ResultadoAuditoria.EXITOSO : resultado);
        entity.setDescripcion(clean(descripcion, 1000));
        entity.setMetadataJson(defaultMetadata(metadataJson));
        entity.setIpAddress(clean(safeContext.ipAddress(), 80));
        entity.setUserAgent(clean(safeContext.userAgent(), 500));
        entity.setRequestId(clean(safeContext.requestId(), 100));
        entity.setCorrelationId(clean(safeContext.correlationId(), 100));
        entity.setEventAt(LocalDateTime.now());

        return entity;
    }

    public AuditoriaFuncionalResponseDto toResponse(AuditoriaFuncional entity) {
        if (entity == null) {
            return null;
        }

        return AuditoriaFuncionalResponseDto.builder()
                .idAuditoria(entity.getIdAuditoria())
                .idUsuarioActorMs1(entity.getIdUsuarioActorMs1())
                .idEmpleadoActorMs2(entity.getIdEmpleadoActorMs2())
                .rolActor(entity.getRolActor())
                .tipoEvento(entity.getTipoEvento())
                .entidad(entity.getEntidad())
                .idRegistroAfectado(entity.getIdRegistroAfectado())
                .accion(entity.getAccion())
                .resultado(entity.getResultado())
                .descripcion(entity.getDescripcion())
                .metadataJson(entity.getMetadataJson())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .requestId(entity.getRequestId())
                .correlationId(entity.getCorrelationId())
                .eventAt(entity.getEventAt())
                .build();
    }

    public List<AuditoriaFuncionalResponseDto> toResponseList(List<AuditoriaFuncional> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(this::toResponse)
                .toList();
    }

    private RolSistema resolveRol(AuditContext context) {
        if (context == null || context.rolActor() == null) {
            return RolSistema.ANONIMO;
        }

        return context.rolActor();
    }

    private String defaultMetadata(String metadataJson) {
        return StringUtils.hasText(metadataJson) ? metadataJson : "{}";
    }

    private String cleanRequired(String value, int maxLength) {
        String cleaned = clean(value, maxLength);

        if (!StringUtils.hasText(cleaned)) {
            throw new IllegalArgumentException("La acción de auditoría es obligatoria.");
        }

        return cleaned;
    }

    private String clean(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String cleaned = value.trim()
                .replaceAll("[\\r\\n\\t]", " ")
                .replaceAll("\\s{2,}", " ");

        return cleaned.substring(0, Math.min(cleaned.length(), maxLength));
    }
}