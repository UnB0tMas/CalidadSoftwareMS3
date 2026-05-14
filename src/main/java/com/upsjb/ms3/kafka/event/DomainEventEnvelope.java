// ruta: src/main/java/com/upsjb/ms3/kafka/event/DomainEventEnvelope.java
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

    public DomainEventEnvelope {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }

        eventType = cleanRequired(eventType, "eventType");
        aggregateId = cleanRequired(aggregateId, "aggregateId");

        if (aggregateType == null) {
            throw new IllegalArgumentException("El aggregateType del evento es obligatorio.");
        }

        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }

        producer = clean(producer);
        if (!StringUtils.hasText(producer)) {
            producer = "ms-catalogo-inventario";
        }

        if (schemaVersion == null || schemaVersion <= 0) {
            schemaVersion = 1;
        }

        requestId = clean(requestId);
        correlationId = clean(correlationId);

        if (payload == null) {
            throw new IllegalArgumentException("El payload del evento es obligatorio.");
        }

        metadata = metadata == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }

    public static <T> DomainEventEnvelope<T> of(
            String eventType,
            AggregateType aggregateType,
            String aggregateId,
            T payload
    ) {
        return new DomainEventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                aggregateType,
                aggregateId,
                LocalDateTime.now(),
                "ms-catalogo-inventario",
                1,
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
        return new DomainEventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                aggregateType,
                aggregateId,
                LocalDateTime.now(),
                "ms-catalogo-inventario",
                1,
                requestId,
                correlationId,
                payload,
                metadata
        );
    }

    private static String cleanRequired(String value, String field) {
        String cleaned = clean(value);
        if (!StringUtils.hasText(cleaned)) {
            throw new IllegalArgumentException("El campo " + field + " del evento es obligatorio.");
        }
        return cleaned;
    }

    private static String clean(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim()
                .replaceAll("[\\r\\n\\t]", " ")
                .replaceAll("\\s{2,}", " ");
    }
}