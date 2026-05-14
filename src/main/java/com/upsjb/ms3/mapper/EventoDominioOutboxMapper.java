// ruta: src/main/java/com/upsjb/ms3/mapper/EventoDominioOutboxMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.EventoDominioOutbox;
import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.EstadoPublicacionEvento;
import com.upsjb.ms3.dto.outbox.response.EventoDominioOutboxResponseDto;
import com.upsjb.ms3.dto.outbox.response.OutboxPublishResultResponseDto;
import com.upsjb.ms3.kafka.outbox.OutboxPublishResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class EventoDominioOutboxMapper {

    private static final int PAYLOAD_PREVIEW_LENGTH = 700;
    private static final int ERROR_MAX_LENGTH = 4000;

    public EventoDominioOutbox toEntity(
            AggregateType aggregateType,
            String aggregateId,
            String eventType,
            String topic,
            String eventKey,
            String payloadJson
    ) {
        return toEntity(
                UUID.randomUUID(),
                aggregateType,
                aggregateId,
                eventType,
                topic,
                eventKey,
                payloadJson
        );
    }

    public EventoDominioOutbox toEntity(
            UUID eventId,
            AggregateType aggregateType,
            String aggregateId,
            String eventType,
            String topic,
            String eventKey,
            String payloadJson
    ) {
        EventoDominioOutbox entity = new EventoDominioOutbox();
        entity.setEventId(eventId == null ? UUID.randomUUID() : eventId);
        entity.setAggregateType(aggregateType);
        entity.setAggregateId(requiredClean(aggregateId, 100, "El aggregateId es obligatorio."));
        entity.setEventType(requiredClean(eventType, 120, "El eventType es obligatorio."));
        entity.setTopic(requiredClean(topic, 200, "El topic es obligatorio."));
        entity.setEventKey(requiredClean(eventKey, 200, "El eventKey es obligatorio."));
        entity.setPayloadJson(requiredPayload(payloadJson));
        entity.setEstadoPublicacion(EstadoPublicacionEvento.PENDIENTE);
        entity.setIntentosPublicacion(0);
        entity.setEstado(Boolean.TRUE);
        entity.setCreatedAt(LocalDateTime.now());

        return entity;
    }

    public EventoDominioOutboxResponseDto toResponse(EventoDominioOutbox entity) {
        return toResponse(entity, true);
    }

    public EventoDominioOutboxResponseDto toResponse(EventoDominioOutbox entity, boolean includePayload) {
        if (entity == null) {
            throw new IllegalArgumentException("El evento outbox es obligatorio para mapear respuesta.");
        }

        return EventoDominioOutboxResponseDto.builder()
                .idEvento(entity.getIdEvento())
                .eventId(entity.getEventId())
                .aggregateType(entity.getAggregateType())
                .aggregateId(entity.getAggregateId())
                .eventType(entity.getEventType())
                .topic(entity.getTopic())
                .eventKey(entity.getEventKey())
                .payloadJson(includePayload ? entity.getPayloadJson() : null)
                .payloadPreview(payloadPreview(entity.getPayloadJson()))
                .estadoPublicacion(entity.getEstadoPublicacion())
                .intentosPublicacion(defaultInteger(entity.getIntentosPublicacion()))
                .errorPublicacion(entity.getErrorPublicacion())
                .reintentable(isReintentable(entity))
                .createdAt(entity.getCreatedAt())
                .publishedAt(entity.getPublishedAt())
                .lockedAt(entity.getLockedAt())
                .lockedBy(entity.getLockedBy())
                .estado(entity.getEstado())
                .build();
    }

    public List<EventoDominioOutboxResponseDto> toResponseList(List<EventoDominioOutbox> entities) {
        return toResponseList(entities, true);
    }

    public List<EventoDominioOutboxResponseDto> toResponseList(
            List<EventoDominioOutbox> entities,
            boolean includePayload
    ) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(entity -> toResponse(entity, includePayload))
                .toList();
    }

    public OutboxPublishResultResponseDto toPublishResultResponse(OutboxPublishResult result) {
        if (result == null) {
            throw new IllegalArgumentException("El resultado de publicación Kafka es obligatorio.");
        }

        return OutboxPublishResultResponseDto.builder()
                .idEvento(result.idEvento())
                .eventId(result.eventId())
                .success(Boolean.TRUE.equals(result.success()))
                .skipped(Boolean.TRUE.equals(result.skipped()))
                .code(result.code())
                .message(result.message())
                .topic(result.topic())
                .eventKey(result.eventKey())
                .partition(result.partition())
                .offset(result.offset())
                .processedAt(result.processedAt())
                .build();
    }

    public void markPublished(EventoDominioOutbox entity, LocalDateTime publishedAt) {
        if (entity == null) {
            return;
        }

        entity.setEstadoPublicacion(EstadoPublicacionEvento.PUBLICADO);
        entity.setPublishedAt(publishedAt == null ? LocalDateTime.now() : publishedAt);
        entity.setErrorPublicacion(null);
        entity.setLockedAt(null);
        entity.setLockedBy(null);
    }

    public void markError(EventoDominioOutbox entity, String errorMessage) {
        if (entity == null) {
            return;
        }

        entity.setEstadoPublicacion(EstadoPublicacionEvento.ERROR);
        entity.setErrorPublicacion(clean(errorMessage, ERROR_MAX_LENGTH));
        entity.setLockedAt(null);
        entity.setLockedBy(null);
    }

    public void markPending(EventoDominioOutbox entity) {
        if (entity == null) {
            return;
        }

        entity.setEstadoPublicacion(EstadoPublicacionEvento.PENDIENTE);
        entity.setErrorPublicacion(null);
        entity.setLockedAt(null);
        entity.setLockedBy(null);
    }

    public void incrementAttempt(EventoDominioOutbox entity) {
        if (entity == null) {
            return;
        }

        entity.setIntentosPublicacion(defaultInteger(entity.getIntentosPublicacion()) + 1);
    }

    public void resetAttempts(EventoDominioOutbox entity) {
        if (entity == null) {
            return;
        }

        entity.setIntentosPublicacion(0);
    }

    public void lock(EventoDominioOutbox entity, String lockedBy, LocalDateTime lockedAt) {
        if (entity == null) {
            return;
        }

        entity.setLockedBy(clean(lockedBy, 100));
        entity.setLockedAt(lockedAt == null ? LocalDateTime.now() : lockedAt);
    }

    public void unlock(EventoDominioOutbox entity) {
        if (entity == null) {
            return;
        }

        entity.setLockedBy(null);
        entity.setLockedAt(null);
    }

    private Boolean isReintentable(EventoDominioOutbox entity) {
        return entity != null
                && entity.getEstadoPublicacion() != null
                && entity.getEstadoPublicacion().isReintentable();
    }

    private String payloadPreview(String payloadJson) {
        if (!StringUtils.hasText(payloadJson)) {
            return null;
        }

        return payloadJson.substring(0, Math.min(payloadJson.length(), PAYLOAD_PREVIEW_LENGTH));
    }

    private String requiredPayload(String payloadJson) {
        if (!StringUtils.hasText(payloadJson)) {
            throw new IllegalArgumentException("El payloadJson del evento outbox es obligatorio.");
        }

        return payloadJson.trim();
    }

    private String requiredClean(String value, int maxLength, String message) {
        String cleaned = clean(value, maxLength);

        if (!StringUtils.hasText(cleaned)) {
            throw new IllegalArgumentException(message);
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

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }
}