// ruta: src/main/java/com/upsjb/ms3/kafka/event/PromocionSnapshotEvent.java
package com.upsjb.ms3.kafka.event;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.PromocionEventType;
import java.util.Map;

public record PromocionSnapshotEvent(
        DomainEventEnvelope<PromocionSnapshotPayload> envelope
) {

    public PromocionSnapshotEvent {
        if (envelope == null) {
            throw new IllegalArgumentException("El envelope del evento de promoción es obligatorio.");
        }
    }

    public static PromocionSnapshotEvent of(
            PromocionEventType eventType,
            Long idPromocion,
            PromocionSnapshotPayload payload
    ) {
        return of(eventType, idPromocion, null, null, payload, Map.of());
    }

    public static PromocionSnapshotEvent of(
            PromocionEventType eventType,
            Long idPromocion,
            String requestId,
            String correlationId,
            PromocionSnapshotPayload payload,
            Map<String, Object> metadata
    ) {
        if (eventType == null) {
            throw new IllegalArgumentException("El tipo de evento de promoción es obligatorio.");
        }

        if (idPromocion == null) {
            throw new IllegalArgumentException("El idPromocion es obligatorio para el evento de promoción.");
        }

        return new PromocionSnapshotEvent(
                DomainEventEnvelope.of(
                        eventType.getCode(),
                        AggregateType.PROMOCION,
                        String.valueOf(idPromocion),
                        requestId,
                        correlationId,
                        payload,
                        metadata
                )
        );
    }

    public PromocionSnapshotPayload payload() {
        return envelope.payload();
    }

    public String eventType() {
        return envelope.eventType();
    }

    public String aggregateId() {
        return envelope.aggregateId();
    }
}