package com.upsjb.ms3.kafka.event;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.Ms4StockEventType;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import java.util.Map;

public record Ms4StockCommandEvent(
        DomainEventEnvelope<Ms4StockCommandPayload> envelope
) {

    public static final int SCHEMA_VERSION =
            1;

    private static final String CONSUMER_CONTRACT =
            "MS4_STOCK_COMMAND_V1";

    private static final String STOCK_STREAM_PREFIX =
            "STOCK_STREAM:";

    public Ms4StockCommandEvent {
        if (envelope == null) {
            throw new IllegalArgumentException(
                    "El envelope del comando de stock MS4 es obligatorio."
            );
        }

        if (envelope.payload() == null) {
            throw new IllegalArgumentException(
                    "El payload del comando de stock MS4 es obligatorio."
            );
        }

        if (
                envelope.schemaVersion() == null
                        || envelope.schemaVersion()
                        != SCHEMA_VERSION
        ) {
            throw new IllegalArgumentException(
                    "El contrato ms4.stock.command.v1 exige schemaVersion=1."
            );
        }

        if (
                envelope.aggregateType()
                        != AggregateType.STOCK
        ) {
            throw new IllegalArgumentException(
                    "El aggregateType del comando de stock MS4 debe ser STOCK."
            );
        }

        Ms4StockEventType envelopeEventType =
                Ms4StockEventType.fromCode(
                        envelope.eventType()
                );

        Ms4StockEventType payloadEventType =
                envelope.payload()
                        .eventType();

        if (
                payloadEventType != null
                        && payloadEventType
                        != envelopeEventType
        ) {
            throw new IllegalArgumentException(
                    "El eventType del envelope no coincide con el eventType del payload."
            );
        }

        Ms4StockCommandPayload normalizedPayload =
                envelope.payload()
                        .withEnvelopeContext(
                                envelope.eventId(),
                                envelope.occurredAt(),
                                envelope.requestId(),
                                envelope.correlationId(),
                                envelopeEventType
                        );

        validateReferences(
                normalizedPayload
        );

        validateMetadataKey(
                envelope.metadata(),
                normalizedPayload
        );

        envelope =
                envelope.withPayload(
                        normalizedPayload,
                        SCHEMA_VERSION,
                        Map.of(
                                "consumerContract",
                                CONSUMER_CONTRACT
                        )
                );
    }

    public static Ms4StockCommandEvent of(
            Ms4StockEventType eventType,
            String referenciaIdExterno,
            Ms4StockCommandPayload payload
    ) {
        return of(
                eventType,
                referenciaIdExterno,
                null,
                null,
                payload,
                Map.of()
        );
    }

    public static Ms4StockCommandEvent of(
            Ms4StockEventType eventType,
            String referenciaIdExterno,
            String requestId,
            String correlationId,
            Ms4StockCommandPayload payload,
            Map<String, Object> metadata
    ) {
        if (eventType == null) {
            throw new IllegalArgumentException(
                    "El tipo de comando MS4 es obligatorio."
            );
        }

        if (
                referenciaIdExterno == null
                        || referenciaIdExterno.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "La referencia externa de MS4 es obligatoria."
            );
        }

        return new Ms4StockCommandEvent(
                DomainEventEnvelope.of(
                        eventType.getCode(),
                        AggregateType.STOCK,
                        referenciaIdExterno,
                        SCHEMA_VERSION,
                        requestId,
                        correlationId,
                        payload,
                        metadata
                )
        );
    }

    public Ms4StockCommandPayload payload() {
        return envelope.payload();
    }

    public String eventType() {
        return envelope.eventType();
    }

    public String aggregateId() {
        return envelope.aggregateId();
    }

    public String expectedKafkaKey() {
        return expectedKafkaKey(
                payload()
        );
    }

    private static void validateReferences(
            Ms4StockCommandPayload payload
    ) {
        Long idSku =
                referenceId(
                        payload.sku()
                );

        Long idAlmacen =
                referenceId(
                        payload.almacen()
                );

        if (
                idSku == null
                        || idSku <= 0
        ) {
            throw new IllegalArgumentException(
                    "El comando de stock MS4 debe incluir un idSku MS3 positivo."
            );
        }

        if (
                idAlmacen == null
                        || idAlmacen <= 0
        ) {
            throw new IllegalArgumentException(
                    "El comando de stock MS4 debe incluir un idAlmacen MS3 positivo."
            );
        }
    }

    private static void validateMetadataKey(
            Map<String, Object> metadata,
            Ms4StockCommandPayload payload
    ) {
        if (
                metadata == null
                        || !metadata.containsKey(
                        "eventKey"
                )
        ) {
            return;
        }

        Object metadataKey =
                metadata.get("eventKey");

        String expectedKey =
                expectedKafkaKey(payload);

        if (
                metadataKey == null
                        || !expectedKey.equals(
                        String.valueOf(
                                metadataKey
                        ).trim()
                )
        ) {
            throw new IllegalArgumentException(
                    "La eventKey declarada en metadata no coincide con el SKU y almacén del payload."
            );
        }
    }

    private static String expectedKafkaKey(
            Ms4StockCommandPayload payload
    ) {
        return STOCK_STREAM_PREFIX
                + referenceId(
                payload.sku()
        )
                + ":"
                + referenceId(
                payload.almacen()
        );
    }

    private static Long referenceId(
            EntityReferenceDto reference
    ) {
        return reference == null
                ? null
                : reference.id();
    }
}