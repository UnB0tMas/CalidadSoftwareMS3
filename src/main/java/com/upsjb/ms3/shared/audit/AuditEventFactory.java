package com.upsjb.ms3.shared.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms3.domain.entity.AuditoriaFuncional;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AuditEventFactory {

    private final ObjectMapper objectMapper;

    public AuditoriaFuncional create(
            TipoEventoAuditoria tipoEvento,
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            AuditResult result,
            String descripcion
    ) {
        return create(tipoEvento, entidad, idRegistroAfectado, accion, result, descripcion, Map.of());
    }

    public AuditoriaFuncional create(
            TipoEventoAuditoria tipoEvento,
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            AuditResult result,
            String descripcion,
            Map<String, Object> metadata
    ) {
        AuditContext context = AuditContextHolder.getOrEmpty();
        return create(context, tipoEvento, entidad, idRegistroAfectado, accion, result, descripcion, metadata);
    }

    public AuditoriaFuncional create(
            AuditContext context,
            TipoEventoAuditoria tipoEvento,
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            AuditResult result,
            String descripcion,
            Map<String, Object> metadata
    ) {
        if (tipoEvento == null) {
            throw new IllegalArgumentException("El tipo de evento de auditoría es obligatorio.");
        }

        if (entidad == null) {
            throw new IllegalArgumentException("La entidad auditada es obligatoria.");
        }

        if (!StringUtils.hasText(accion)) {
            throw new IllegalArgumentException("La acción de auditoría es obligatoria.");
        }

        AuditContext safeContext = context == null ? AuditContext.empty() : context;

        AuditoriaFuncional audit = new AuditoriaFuncional();
        audit.setIdUsuarioActorMs1(safeContext.idUsuarioActorMs1());
        audit.setIdEmpleadoActorMs2(safeContext.idEmpleadoActorMs2());
        audit.setRolActor(resolveRol(safeContext));
        audit.setTipoEvento(tipoEvento);
        audit.setEntidad(entidad);
        audit.setIdRegistroAfectado(clean(idRegistroAfectado, 100));
        audit.setAccion(cleanRequired(accion, 120));
        audit.setResultado(result == null ? AuditResult.SUCCESS.toResultadoAuditoria() : result.toResultadoAuditoria());
        audit.setDescripcion(clean(descripcion, 1000));
        audit.setMetadataJson(toJson(mergeMetadata(safeContext, metadata)));
        audit.setIpAddress(clean(safeContext.ipAddress(), 80));
        audit.setUserAgent(clean(safeContext.userAgent(), 500));
        audit.setRequestId(clean(safeContext.requestId(), 100));
        audit.setCorrelationId(clean(safeContext.correlationId(), 100));
        audit.setEventAt(LocalDateTime.now());

        return audit;
    }

    private Map<String, Object> mergeMetadata(AuditContext context, Map<String, Object> metadata) {
        AuditMetadataBuilder builder = AuditMetadataBuilder.create()
                .request(context);

        if (metadata != null) {
            metadata.forEach(builder::put);
        }

        return builder.build();
    }

    private String toJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }

        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            return "{\"metadataSerializationError\":\"No se pudo serializar metadata de auditoría.\"}";
        }
    }

    private RolSistema resolveRol(AuditContext context) {
        if (context.rolActor() != null) {
            return context.rolActor();
        }

        return context.hasActor() ? RolSistema.CLIENTE : RolSistema.ANONIMO;
    }

    private String cleanRequired(String value, int maxLength) {
        String cleaned = clean(value, maxLength);

        if (!StringUtils.hasText(cleaned)) {
            throw new IllegalArgumentException("El valor requerido de auditoría está vacío.");
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