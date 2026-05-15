// ruta: src/main/java/com/upsjb/ms3/kafka/event/PrecioSnapshotEvent.java
package com.upsjb.ms3.kafka.event;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.PrecioEventType;
import java.util.Map;

public record PrecioSnapshotEvent(
        DomainEventEnvelope<PrecioSnapshotPayload> envelope
) {

    public PrecioSnapshotEvent {
        if (envelope == null) {
            throw new IllegalArgumentException("El envelope del evento de precio es obligatorio.");
        }
    }

    public static PrecioSnapshotEvent of(
            PrecioEventType eventType,
            Long idSku,
            PrecioSnapshotPayload payload
    ) {
        return of(eventType, idSku, null, null, payload, Map.of());
    }

    public static PrecioSnapshotEvent of(
            PrecioEventType eventType,
            Long idSku,
            String requestId,
            String correlationId,
            PrecioSnapshotPayload payload,
            Map<String, Object> metadata
    ) {
        if (eventType == null) {
            throw new IllegalArgumentException("El tipo de evento de precio es obligatorio.");
        }

        if (idSku == null) {
            throw new IllegalArgumentException("El idSku es obligatorio para el evento de precio.");
        }

        return new PrecioSnapshotEvent(
                DomainEventEnvelope.of(
                        eventType.getCode(),
                        AggregateType.PRECIO,
                        String.valueOf(idSku),
                        requestId,
                        correlationId,
                        payload,
                        metadata
                )
        );
    }

    public PrecioSnapshotPayload payload() {
        return envelope.payload();
    }

    public String eventType() {
        return envelope.eventType();
    }

    public String aggregateId() {
        return envelope.aggregateId();
    }
}