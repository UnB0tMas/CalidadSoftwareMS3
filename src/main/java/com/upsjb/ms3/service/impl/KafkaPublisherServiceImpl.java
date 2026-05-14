// ruta: src/main/java/com/upsjb/ms3/service/impl/KafkaPublisherServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.outbox.request.OutboxRetryRequestDto;
import com.upsjb.ms3.dto.outbox.response.OutboxPublishResultResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.kafka.outbox.OutboxEventPublisher;
import com.upsjb.ms3.kafka.outbox.OutboxPublishResult;
import com.upsjb.ms3.mapper.EventoDominioOutboxMapper;
import com.upsjb.ms3.policy.OutboxPolicy;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.KafkaPublisherService;
import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPublisherServiceImpl implements KafkaPublisherService {

    private final OutboxEventPublisher outboxEventPublisher;
    private final EventoDominioOutboxMapper eventoDominioOutboxMapper;
    private final OutboxPolicy outboxPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<OutboxPublishResultResponseDto> publicar(Long idEvento) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        outboxPolicy.ensureCanForcePublish(actor);

        OutboxPublishResultResponseDto result = publicarInterno(idEvento);

        return apiResponseFactory.dtoOk(
                Boolean.TRUE.equals(result.success())
                        ? "Evento publicado correctamente en Kafka."
                        : result.message(),
                result
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<OutboxPublishResultResponseDto> reintentar(
            Long idEvento,
            OutboxRetryRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        outboxPolicy.ensureCanRetryOutbox(actor);

        OutboxRetryRequestDto normalized = normalizeRetryRequest(request);

        if (Boolean.TRUE.equals(normalized.forzarReintento())) {
            outboxPolicy.ensureCanForcePublish(actor);
        }

        OutboxPublishResultResponseDto result = reintentarInterno(
                idEvento,
                Boolean.TRUE.equals(normalized.forzarReintento())
        );

        return apiResponseFactory.dtoOk(
                Boolean.TRUE.equals(result.success())
                        ? "Evento reintentado correctamente."
                        : result.message(),
                result
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<List<OutboxPublishResultResponseDto>> publicarPendientes() {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        outboxPolicy.ensureCanForcePublish(actor);

        List<OutboxPublishResultResponseDto> results = publicarPendientesInterno();

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                results
        );
    }

    @Override
    @Transactional
    public OutboxPublishResultResponseDto publicarInterno(Long idEvento) {
        validarIdEvento(idEvento);

        OutboxPublishResult result = outboxEventPublisher.publishLocked(idEvento);
        OutboxPublishResultResponseDto response = eventoDominioOutboxMapper.toPublishResultResponse(result);

        auditarResultadoPublicacion(response, "PUBLICAR_EVENTO_KAFKA");

        logPublicationResult(response, "PUBLICAR_EVENTO_KAFKA");

        return response;
    }

    @Override
    @Transactional
    public OutboxPublishResultResponseDto reintentarInterno(Long idEvento, boolean forzarReintento) {
        validarIdEvento(idEvento);

        OutboxPublishResult result = outboxEventPublisher.retry(idEvento, forzarReintento);
        OutboxPublishResultResponseDto response = eventoDominioOutboxMapper.toPublishResultResponse(result);

        auditarReintento(response, forzarReintento);
        auditarResultadoPublicacion(response, "REINTENTAR_EVENTO_KAFKA");

        logPublicationResult(response, "REINTENTAR_EVENTO_KAFKA");

        return response;
    }

    @Override
    @Transactional
    public List<OutboxPublishResultResponseDto> publicarPendientesInterno() {
        List<OutboxPublishResult> results = outboxEventPublisher.publishNextBatch();

        List<OutboxPublishResultResponseDto> response = results.stream()
                .map(eventoDominioOutboxMapper::toPublishResultResponse)
                .toList();

        response.forEach(result -> {
            auditarResultadoPublicacion(result, "PUBLICAR_LOTE_EVENTOS_KAFKA");
            logPublicationResult(result, "PUBLICAR_LOTE_EVENTOS_KAFKA");
        });

        return response;
    }

    private void validarIdEvento(Long idEvento) {
        if (idEvento == null) {
            throw new ValidationException(
                    "OUTBOX_ID_REQUERIDO",
                    "El identificador del evento outbox es obligatorio."
            );
        }
    }

    private OutboxRetryRequestDto normalizeRetryRequest(OutboxRetryRequestDto request) {
        if (request == null || !StringUtils.hasText(request.motivo())) {
            throw new ValidationException(
                    "OUTBOX_REINTENTO_MOTIVO_REQUERIDO",
                    "No se puede reintentar evento Kafka sin motivo."
            );
        }

        return OutboxRetryRequestDto.builder()
                .motivo(request.motivo().trim())
                .forzarReintento(Boolean.TRUE.equals(request.forzarReintento()))
                .build();
    }

    private void auditarReintento(OutboxPublishResultResponseDto result, boolean forzarReintento) {
        registrarAuditoriaExito(
                TipoEventoAuditoria.EVENTO_KAFKA_REINTENTADO,
                result,
                "REINTENTAR_EVENTO_KAFKA",
                "Evento Kafka reintentado correctamente.",
                metadata(result, Map.of("forzarReintento", forzarReintento))
        );
    }

    private void auditarResultadoPublicacion(OutboxPublishResultResponseDto result, String accion) {
        if (Boolean.TRUE.equals(result.success())) {
            registrarAuditoriaExito(
                    TipoEventoAuditoria.EVENTO_KAFKA_PUBLICADO,
                    result,
                    accion,
                    "Evento publicado correctamente en Kafka.",
                    metadata(result, Map.of())
            );
            return;
        }

        registrarAuditoriaFallo(
                TipoEventoAuditoria.EVENTO_KAFKA_FALLIDO,
                result,
                accion,
                result.message(),
                metadata(result, Map.of())
        );
    }

    private void registrarAuditoriaExito(
            TipoEventoAuditoria tipoEvento,
            OutboxPublishResultResponseDto result,
            String accion,
            String descripcion,
            Map<String, Object> metadata
    ) {
        boolean hasContext = AuditContextHolder.hasContext();

        if (!hasContext) {
            AuditContextHolder.set(AuditContext.system());
        }

        try {
            auditoriaFuncionalService.registrarExito(
                    tipoEvento,
                    EntidadAuditada.EVENTO_DOMINIO_OUTBOX,
                    result.idEvento() == null ? null : String.valueOf(result.idEvento()),
                    accion,
                    descripcion,
                    metadata == null ? Map.of() : metadata
            );
        } finally {
            if (!hasContext) {
                AuditContextHolder.clear();
            }
        }
    }

    private void registrarAuditoriaFallo(
            TipoEventoAuditoria tipoEvento,
            OutboxPublishResultResponseDto result,
            String accion,
            String descripcion,
            Map<String, Object> metadata
    ) {
        boolean hasContext = AuditContextHolder.hasContext();

        if (!hasContext) {
            AuditContextHolder.set(AuditContext.system());
        }

        try {
            auditoriaFuncionalService.registrarFallo(
                    tipoEvento,
                    EntidadAuditada.EVENTO_DOMINIO_OUTBOX,
                    result.idEvento() == null ? null : String.valueOf(result.idEvento()),
                    accion,
                    descripcion,
                    metadata == null ? Map.of() : metadata
            );
        } finally {
            if (!hasContext) {
                AuditContextHolder.clear();
            }
        }
    }

    private Map<String, Object> metadata(
            OutboxPublishResultResponseDto result,
            Map<String, Object> extra
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("idEvento", result.idEvento());
        metadata.put("eventId", result.eventId());
        metadata.put("topic", result.topic());
        metadata.put("eventKey", result.eventKey());
        metadata.put("success", Boolean.TRUE.equals(result.success()));
        metadata.put("skipped", Boolean.TRUE.equals(result.skipped()));
        metadata.put("code", result.code());
        metadata.put("message", result.message());
        metadata.put("partition", result.partition());
        metadata.put("offset", result.offset());
        metadata.put("processedAt", result.processedAt());

        if (extra != null) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private void logPublicationResult(OutboxPublishResultResponseDto result, String action) {
        if (Boolean.TRUE.equals(result.success())) {
            log.info(
                    "Kafka outbox publicado. action={}, idEvento={}, eventId={}, topic={}, eventKey={}, partition={}, offset={}",
                    action,
                    result.idEvento(),
                    result.eventId(),
                    result.topic(),
                    result.eventKey(),
                    result.partition(),
                    result.offset()
            );
            return;
        }

        log.warn(
                "Kafka outbox no publicado. action={}, idEvento={}, eventId={}, topic={}, eventKey={}, skipped={}, code={}, message={}",
                action,
                result.idEvento(),
                result.eventId(),
                result.topic(),
                result.eventKey(),
                result.skipped(),
                result.code(),
                result.message()
        );
    }
}