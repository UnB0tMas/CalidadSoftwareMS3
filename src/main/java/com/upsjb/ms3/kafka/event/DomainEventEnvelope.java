package com.upsjb.ms3.kafka.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.upsjb.ms3.domain.enums.AggregateType;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.util.StringUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DomainEventEnvelope<T>(
        UUID eventId,
        String eventType,
        AggregateType aggregateType,
        String aggregateId,
        LocalDateTime occurredAt,
        String producer,
        Integer schemaVersion,
        String requestId,
        String correlationId,
        T payload,
        Map<String, Object> metadata
) {

    public static final String DEFAULT_PRODUCER =
            "ms-catalogo-inventario";

    public static final int DEFAULT_SCHEMA_VERSION = 1;

    public DomainEventEnvelope {
        eventId = eventId == null
                ? UUID.randomUUID()
                : eventId;

        eventType = cleanRequired(
                eventType,
                "eventType"
        );

        aggregateId = cleanRequired(
                aggregateId,
                "aggregateId"
        );

        if (aggregateType == null) {
            throw new IllegalArgumentException(
                    "El aggregateType del evento es obligatorio."
            );
        }

        occurredAt = occurredAt == null
                ? LocalDateTime.now()
                : occurredAt;

        producer = clean(producer);

        if (!StringUtils.hasText(producer)) {
            producer = DEFAULT_PRODUCER;
        }

        if (
                schemaVersion == null
                        || schemaVersion <= 0
        ) {
            schemaVersion = DEFAULT_SCHEMA_VERSION;
        }

        requestId = clean(requestId);
        correlationId = clean(correlationId);

        if (payload == null) {
            throw new IllegalArgumentException(
                    "El payload del evento es obligatorio."
            );
        }

        metadata = metadata == null
                ? Map.of()
                : Collections.unmodifiableMap(
                new LinkedHashMap<>(
                        metadata
                )
        );
    }

    public static <T> DomainEventEnvelope<T> of(
            String eventType,
            AggregateType aggregateType,
            String aggregateId,
            T payload
    ) {
        return of(
                eventType,
                aggregateType,
                aggregateId,
                DEFAULT_SCHEMA_VERSION,
                null,
                null,
                payload,
                Map.of()
        );
    }

    public static <T> DomainEventEnvelope<T> of(
            String eventType,
            AggregateType aggregateType,
            String aggregateId,
            String requestId,
            String correlationId,
            T payload,
            Map<String, Object> metadata
    ) {
        return of(
                eventType,
                aggregateType,
                aggregateId,
                DEFAULT_SCHEMA_VERSION,
                requestId,
                correlationId,
                payload,
                metadata
        );
    }

    public static <T> DomainEventEnvelope<T> of(
            String eventType,
            AggregateType aggregateType,
            String aggregateId,
            Integer schemaVersion,
            String requestId,
            String correlationId,
            T payload,
            Map<String, Object> metadata
    ) {
        return new DomainEventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                aggregateType,
                aggregateId,
                LocalDateTime.now(),
                DEFAULT_PRODUCER,
                schemaVersion,
                requestId,
                correlationId,
                payload,
                metadata
        );
    }

    public <R> DomainEventEnvelope<R> withPayload(
            R newPayload,
            Integer newSchemaVersion,
            Map<String, Object> additionalMetadata
    ) {
        return withPayload(
                newPayload,
                aggregateType,
                aggregateId,
                eventType,
                newSchemaVersion,
                additionalMetadata
        );
    }

    public <R> DomainEventEnvelope<R> withPayload(
            R newPayload,
            AggregateType newAggregateType,
            String newAggregateId,
            String newEventType,
            Integer newSchemaVersion,
            Map<String, Object> additionalMetadata
    ) {
        Map<String, Object> mergedMetadata =
                new LinkedHashMap<>(
                        metadata
                );

        if (additionalMetadata != null) {
            additionalMetadata.forEach(
                    (key, value) -> {
                        if (
                                StringUtils.hasText(key)
                                        && value != null
                        ) {
                            mergedMetadata.put(
                                    key.trim(),
                                    value
                            );
                        }
                    }
            );
        }

        return new DomainEventEnvelope<>(
                eventId,
                StringUtils.hasText(newEventType)
                        ? newEventType
                        : eventType,
                newAggregateType == null
                        ? aggregateType
                        : newAggregateType,
                StringUtils.hasText(newAggregateId)
                        ? newAggregateId
                        : aggregateId,
                occurredAt,
                producer,
                newSchemaVersion,
                requestId,
                correlationId,
                newPayload,
                mergedMetadata
        );
    }

    private static String cleanRequired(
            String value,
            String field
    ) {
        String cleaned = clean(value);

        if (!StringUtils.hasText(cleaned)) {
            throw new IllegalArgumentException(
                    "El campo "
                            + field
                            + " del evento es obligatorio."
            );
        }

        return cleaned;
    }

    private static String clean(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim()
                .replaceAll(
                        "[\\r\\n\\t]",
                        " "
                )
                .replaceAll(
                        "\\s{2,}",
                        " "
                );
    }
}