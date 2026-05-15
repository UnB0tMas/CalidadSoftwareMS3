// ruta: src/main/java/com/upsjb/ms3/service/impl/EventoDominioOutboxServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.EventoDominioOutbox;
import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.outbox.filter.EventoDominioOutboxFilterDto;
import com.upsjb.ms3.dto.outbox.request.OutboxRetryRequestDto;
import com.upsjb.ms3.dto.outbox.response.EventoDominioOutboxResponseDto;
import com.upsjb.ms3.dto.outbox.response.OutboxPublishResultResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.kafka.event.DomainEventEnvelope;
import com.upsjb.ms3.kafka.event.MovimientoInventarioEvent;
import com.upsjb.ms3.kafka.event.PrecioSnapshotEvent;
import com.upsjb.ms3.kafka.event.ProductoSnapshotEvent;
import com.upsjb.ms3.kafka.event.PromocionSnapshotEvent;
import com.upsjb.ms3.kafka.event.StockSnapshotEvent;
import com.upsjb.ms3.kafka.outbox.OutboxEventFactory;
import com.upsjb.ms3.mapper.EventoDominioOutboxMapper;
import com.upsjb.ms3.policy.OutboxPolicy;
import com.upsjb.ms3.repository.EventoDominioOutboxRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.service.contract.KafkaPublisherService;
import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.EventoDominioOutboxSpecifications;
import com.upsjb.ms3.validator.EventoDominioOutboxValidator;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventoDominioOutboxServiceImpl implements EventoDominioOutboxService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idEvento",
            "eventId",
            "aggregateType",
            "aggregateId",
            "eventType",
            "topic",
            "eventKey",
            "estadoPublicacion",
            "intentosPublicacion",
            "createdAt",
            "publishedAt",
            "lockedAt",
            "lockedBy",
            "estado"
    );

    private final EventoDominioOutboxRepository eventoDominioOutboxRepository;
    private final EventoDominioOutboxMapper eventoDominioOutboxMapper;
    private final EventoDominioOutboxValidator eventoDominioOutboxValidator;
    private final OutboxEventFactory outboxEventFactory;
    private final OutboxPolicy outboxPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final KafkaPublisherService kafkaPublisherService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public EventoDominioOutboxResponseDto registrarEvento(
            AggregateType aggregateType,
            String aggregateId,
            String eventType,
            Object payload
    ) {
        EventoDominioOutbox event = outboxEventFactory.create(
                aggregateType,
                aggregateId,
                eventType,
                payload
        );

        return persistirNuevoEvento(event);
    }

    @Override
    @Transactional
    public EventoDominioOutboxResponseDto registrarEvento(DomainEventEnvelope<?> envelope) {
        EventoDominioOutbox event = outboxEventFactory.createEnvelope(envelope);
        return persistirNuevoEvento(event);
    }

    @Override
    @Transactional
    public EventoDominioOutboxResponseDto registrarEvento(ProductoSnapshotEvent event) {
        return persistirNuevoEvento(outboxEventFactory.create(event));
    }

    @Override
    @Transactional
    public EventoDominioOutboxResponseDto registrarEvento(PrecioSnapshotEvent event) {
        return persistirNuevoEvento(outboxEventFactory.create(event));
    }

    @Override
    @Transactional
    public EventoDominioOutboxResponseDto registrarEvento(PromocionSnapshotEvent event) {
        return persistirNuevoEvento(outboxEventFactory.create(event));
    }

    @Override
    @Transactional
    public EventoDominioOutboxResponseDto registrarEvento(StockSnapshotEvent event) {
        return persistirNuevoEvento(outboxEventFactory.create(event));
    }

    @Override
    @Transactional
    public EventoDominioOutboxResponseDto registrarEvento(MovimientoInventarioEvent event) {
        return persistirNuevoEvento(outboxEventFactory.create(event));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<EventoDominioOutboxResponseDto>> listar(
            EventoDominioOutboxFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        outboxPolicy.ensureCanViewOutbox(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "createdAt");
        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "createdAt"
        );

        PageResponseDto<EventoDominioOutboxResponseDto> response = paginationService.toPageResponseDto(
                eventoDominioOutboxRepository.findAll(
                        EventoDominioOutboxSpecifications.fromFilter(filter),
                        pageable
                ),
                event -> eventoDominioOutboxMapper.toResponse(event, false)
        );

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EventoDominioOutboxResponseDto> obtenerDetalle(Long idEvento, Boolean incluirPayload) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        outboxPolicy.ensureCanViewOutbox(actor);

        boolean includePayload = Boolean.TRUE.equals(incluirPayload);
        if (includePayload) {
            outboxPolicy.ensureCanViewPayload(actor);
            eventoDominioOutboxValidator.validateCanInspectPayload(true);
        }

        EventoDominioOutbox event = findRequired(idEvento);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                eventoDominioOutboxMapper.toResponse(event, includePayload)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EventoDominioOutboxResponseDto> obtenerPorEventId(UUID eventId, Boolean incluirPayload) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        outboxPolicy.ensureCanViewOutbox(actor);

        if (eventId == null) {
            throw new ValidationException(
                    "OUTBOX_EVENT_ID_REQUERIDO",
                    "El eventId del evento outbox es obligatorio."
            );
        }

        boolean includePayload = Boolean.TRUE.equals(incluirPayload);
        if (includePayload) {
            outboxPolicy.ensureCanViewPayload(actor);
            eventoDominioOutboxValidator.validateCanInspectPayload(true);
        }

        EventoDominioOutbox event = eventoDominioOutboxRepository.findByEventIdAndEstadoTrue(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "OUTBOX_EVENTO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                eventoDominioOutboxMapper.toResponse(event, includePayload)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<EventoDominioOutboxResponseDto> marcarPublicado(Long idEvento) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        outboxPolicy.ensureCanForcePublish(actor);

        EventoDominioOutbox event = findRequiredForUpdate(idEvento);
        eventoDominioOutboxMapper.markPublished(event, LocalDateTime.now());
        EventoDominioOutbox saved = eventoDominioOutboxRepository.save(event);

        registrarAuditoriaExito(
                TipoEventoAuditoria.EVENTO_KAFKA_PUBLICADO,
                saved,
                "MARCAR_EVENTO_OUTBOX_PUBLICADO",
                "Evento Kafka marcado como publicado correctamente.",
                eventAuditMetadata(saved)
        );

        log.info(
                "Evento outbox marcado como publicado. idEvento={}, eventId={}, actor={}",
                saved.getIdEvento(),
                saved.getEventId(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoOk(
                "Evento publicado correctamente.",
                eventoDominioOutboxMapper.toResponse(saved, false)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<EventoDominioOutboxResponseDto> marcarError(Long idEvento, String errorPublicacion) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        outboxPolicy.ensureCanForcePublish(actor);

        String safeError = requireErrorMessage(errorPublicacion);
        EventoDominioOutbox event = findRequiredForUpdate(idEvento);
        eventoDominioOutboxMapper.markError(event, safeError);
        EventoDominioOutbox saved = eventoDominioOutboxRepository.save(event);

        registrarAuditoriaExito(
                TipoEventoAuditoria.EVENTO_KAFKA_FALLIDO,
                saved,
                "MARCAR_EVENTO_OUTBOX_ERROR",
                "Evento Kafka marcado en error correctamente.",
                eventAuditMetadata(saved)
        );

        log.warn(
                "Evento outbox marcado como error. idEvento={}, eventId={}, actor={}, error={}",
                saved.getIdEvento(),
                saved.getEventId(),
                actor.actorLabel(),
                safeError
        );

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                eventoDominioOutboxMapper.toResponse(saved, false)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<EventoDominioOutboxResponseDto> incrementarIntento(Long idEvento) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        outboxPolicy.ensureCanForcePublish(actor);

        EventoDominioOutbox event = findRequiredForUpdate(idEvento);
        eventoDominioOutboxMapper.incrementAttempt(event);
        EventoDominioOutbox saved = eventoDominioOutboxRepository.save(event);

        registrarAuditoriaExito(
                TipoEventoAuditoria.EVENTO_KAFKA_REINTENTADO,
                saved,
                "INCREMENTAR_INTENTO_EVENTO_OUTBOX",
                "Intento de publicación incrementado correctamente.",
                eventAuditMetadata(saved)
        );

        log.info(
                "Intento de evento outbox incrementado. idEvento={}, eventId={}, intentos={}, actor={}",
                saved.getIdEvento(),
                saved.getEventId(),
                saved.getIntentosPublicacion(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                eventoDominioOutboxMapper.toResponse(saved, false)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<EventoDominioOutboxResponseDto> prepararReintento(Long idEvento, OutboxRetryRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        outboxPolicy.ensureCanRetryOutbox(actor);

        OutboxRetryRequestDto normalized = normalizeRetryRequest(request);
        if (Boolean.TRUE.equals(normalized.forzarReintento())) {
            outboxPolicy.ensureCanForcePublish(actor);
        }

        EventoDominioOutbox event = findRequiredForUpdate(idEvento);
        eventoDominioOutboxValidator.validateCanRetry(event, Boolean.TRUE.equals(normalized.forzarReintento()));
        eventoDominioOutboxMapper.markPending(event);

        if (Boolean.TRUE.equals(normalized.forzarReintento())) {
            eventoDominioOutboxMapper.resetAttempts(event);
        }

        EventoDominioOutbox saved = eventoDominioOutboxRepository.save(event);

        registrarAuditoriaExito(
                TipoEventoAuditoria.EVENTO_KAFKA_REINTENTADO,
                saved,
                "PREPARAR_REINTENTO_EVENTO_OUTBOX",
                "Evento Kafka preparado para reintento correctamente.",
                retryPreparationAuditMetadata(saved, normalized, actor)
        );

        log.info(
                "Evento outbox preparado para reintento. idEvento={}, eventId={}, actor={}, forzado={}",
                saved.getIdEvento(),
                saved.getEventId(),
                actor.actorLabel(),
                normalized.forzarReintento()
        );

        return apiResponseFactory.dtoOk(
                "Evento preparado para reintento correctamente.",
                eventoDominioOutboxMapper.toResponse(saved, false)
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

        EventoDominioOutbox event = findRequired(idEvento);
        eventoDominioOutboxValidator.validateCanRetry(event, Boolean.TRUE.equals(normalized.forzarReintento()));

        OutboxPublishResultResponseDto result = kafkaPublisherService.reintentarInterno(
                idEvento,
                Boolean.TRUE.equals(normalized.forzarReintento())
        );

        registrarAuditoriaExito(
                TipoEventoAuditoria.EVENTO_KAFKA_REINTENTADO,
                event,
                "REINTENTAR_EVENTO_OUTBOX",
                "Evento Kafka reintentado correctamente.",
                retryAuditMetadata(event, result, normalized, actor)
        );

        log.info(
                "Evento outbox reintentado. idEvento={}, eventId={}, aggregateType={}, aggregateId={}, actor={}, success={}, skipped={}",
                event.getIdEvento(),
                event.getEventId(),
                event.getAggregateType(),
                event.getAggregateId(),
                actor.actorLabel(),
                result.success(),
                result.skipped()
        );

        return apiResponseFactory.dtoOk(
                Boolean.TRUE.equals(result.success())
                        ? "Evento reintentado correctamente."
                        : result.message(),
                result
        );
    }

    private EventoDominioOutboxResponseDto persistirNuevoEvento(EventoDominioOutbox event) {
        eventoDominioOutboxValidator.requireActive(event);
        eventoDominioOutboxValidator.validateUniqueEventId(
                event.getEventId(),
                eventoDominioOutboxRepository.existsByEventIdAndEstadoTrue(event.getEventId())
        );

        EventoDominioOutbox saved = eventoDominioOutboxRepository.save(event);

        registrarAuditoriaExito(
                TipoEventoAuditoria.EVENTO_KAFKA_REGISTRADO,
                saved,
                "REGISTRAR_EVENTO_OUTBOX",
                "Evento Kafka registrado correctamente.",
                eventAuditMetadata(saved)
        );

        log.info(
                "Evento outbox registrado. idEvento={}, eventId={}, aggregateType={}, aggregateId={}, eventType={}, topic={}",
                saved.getIdEvento(),
                saved.getEventId(),
                saved.getAggregateType(),
                saved.getAggregateId(),
                saved.getEventType(),
                saved.getTopic()
        );

        return eventoDominioOutboxMapper.toResponse(saved, false);
    }

    private EventoDominioOutbox findRequired(Long idEvento) {
        if (idEvento == null) {
            throw new ValidationException(
                    "OUTBOX_ID_REQUERIDO",
                    "El identificador del evento outbox es obligatorio."
            );
        }

        EventoDominioOutbox event = eventoDominioOutboxRepository.findByIdEventoAndEstadoTrue(idEvento)
                .orElseThrow(() -> new NotFoundException(
                        "OUTBOX_EVENTO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        eventoDominioOutboxValidator.requireActive(event);
        return event;
    }

    private EventoDominioOutbox findRequiredForUpdate(Long idEvento) {
        if (idEvento == null) {
            throw new ValidationException(
                    "OUTBOX_ID_REQUERIDO",
                    "El identificador del evento outbox es obligatorio."
            );
        }

        EventoDominioOutbox event = eventoDominioOutboxRepository.findActivoByIdForUpdate(idEvento)
                .orElseThrow(() -> new NotFoundException(
                        "OUTBOX_EVENTO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        eventoDominioOutboxValidator.requireActive(event);
        return event;
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

    private String requireErrorMessage(String errorPublicacion) {
        if (!StringUtils.hasText(errorPublicacion)) {
            throw new ValidationException(
                    "OUTBOX_ERROR_REQUERIDO",
                    "Debe indicar el error de publicación del evento."
            );
        }

        String cleaned = errorPublicacion.trim()
                .replaceAll("[\\r\\n\\t]", " ")
                .replaceAll("\\s{2,}", " ");

        return cleaned.substring(0, Math.min(cleaned.length(), 4000));
    }

    private PageRequestDto safePageRequest(PageRequestDto pageRequest, String defaultSortBy) {
        if (pageRequest == null) {
            return PageRequestDto.builder()
                    .page(0)
                    .size(20)
                    .sortBy(defaultSortBy)
                    .sortDirection("DESC")
                    .build();
        }

        return pageRequest;
    }

    private void registrarAuditoriaExito(
            TipoEventoAuditoria tipoEvento,
            EventoDominioOutbox event,
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
                    event == null || event.getIdEvento() == null ? null : String.valueOf(event.getIdEvento()),
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

    private Map<String, Object> eventAuditMetadata(EventoDominioOutbox event) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("idEvento", event.getIdEvento());
        metadata.put("eventId", event.getEventId());
        metadata.put("aggregateType", event.getAggregateType() == null ? null : event.getAggregateType().getCode());
        metadata.put("aggregateId", event.getAggregateId());
        metadata.put("eventType", event.getEventType());
        metadata.put("topic", event.getTopic());
        metadata.put("eventKey", event.getEventKey());
        metadata.put("estadoPublicacion", event.getEstadoPublicacion() == null ? null : event.getEstadoPublicacion().getCode());
        metadata.put("intentosPublicacion", event.getIntentosPublicacion());
        metadata.put("lockedBy", event.getLockedBy());
        metadata.put("lockedAt", event.getLockedAt());
        return metadata;
    }

    private Map<String, Object> retryPreparationAuditMetadata(
            EventoDominioOutbox event,
            OutboxRetryRequestDto request,
            AuthenticatedUserContext actor
    ) {
        Map<String, Object> metadata = eventAuditMetadata(event);
        metadata.put("motivo", request.motivo());
        metadata.put("forzarReintento", Boolean.TRUE.equals(request.forzarReintento()));
        metadata.put("actor", actor.actorLabel());
        return metadata;
    }

    private Map<String, Object> retryAuditMetadata(
            EventoDominioOutbox event,
            OutboxPublishResultResponseDto result,
            OutboxRetryRequestDto request,
            AuthenticatedUserContext actor
    ) {
        Map<String, Object> metadata = eventAuditMetadata(event);
        metadata.put("motivo", request.motivo());
        metadata.put("forzarReintento", Boolean.TRUE.equals(request.forzarReintento()));
        metadata.put("resultadoCodigo", result.code());
        metadata.put("resultadoMensaje", result.message());
        metadata.put("publicacionExitosa", Boolean.TRUE.equals(result.success()));
        metadata.put("publicacionOmitida", Boolean.TRUE.equals(result.skipped()));
        metadata.put("actor", actor.actorLabel());
        return metadata;
    }
}