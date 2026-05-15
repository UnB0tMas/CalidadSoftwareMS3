// ruta: src/main/java/com/upsjb/ms3/kafka/event/ProductoSnapshotEvent.java
package com.upsjb.ms3.kafka.event;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.ProductoEventType;
import java.util.Map;

public record ProductoSnapshotEvent(
        DomainEventEnvelope<ProductoSnapshotPayload> envelope
) {

    public ProductoSnapshotEvent {
        if (envelope == null) {
            throw new IllegalArgumentException("El envelope del evento de producto es obligatorio.");
        }
    }

    public static ProductoSnapshotEvent of(
            ProductoEventType eventType,
            Long idProducto,
            ProductoSnapshotPayload payload
    ) {
        return of(eventType, idProducto, null, null, payload, Map.of());
    }

    public static ProductoSnapshotEvent of(
            ProductoEventType eventType,
            Long idProducto,
            String requestId,
            String correlationId,
            ProductoSnapshotPayload payload,
            Map<String, Object> metadata
    ) {
        if (eventType == null) {
            throw new IllegalArgumentException("El tipo de evento de producto es obligatorio.");
        }

        if (idProducto == null) {
            throw new IllegalArgumentException("El idProducto es obligatorio para el evento de producto.");
        }

        return new ProductoSnapshotEvent(
                DomainEventEnvelope.of(
                        eventType.getCode(),
                        AggregateType.PRODUCTO,
                        String.valueOf(idProducto),
                        requestId,
                        correlationId,
                        payload,
                        metadata
                )
        );
    }

    public ProductoSnapshotPayload payload() {
        return envelope.payload();
    }

    public String eventType() {
        return envelope.eventType();
    }

    public String aggregateId() {
        return envelope.aggregateId();
    }
}