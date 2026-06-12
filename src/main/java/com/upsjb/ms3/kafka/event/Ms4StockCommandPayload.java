package com.upsjb.ms3.kafka.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.upsjb.ms3.domain.enums.Ms4StockEventType;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ms4StockCommandPayload(
        String eventId,
        String idempotencyKey,
        Ms4StockEventType eventType,
        EntityReferenceDto sku,
        EntityReferenceDto almacen,
        TipoReferenciaStock referenciaTipo,
        String referenciaIdExterno,
        Integer cantidad,
        String codigoReserva,
        Long actorIdUsuarioMs1,
        Long actorIdEmpleadoMs2,
        RolSistema actorRol,
        LocalDateTime occurredAt,
        LocalDateTime expiresAt,
        String motivo,
        String requestId,
        String correlationId,
        String metadataJson
) {

    public Ms4StockCommandPayload withEnvelopeContext(
            UUID envelopeEventId,
            LocalDateTime envelopeOccurredAt,
            String envelopeRequestId,
            String envelopeCorrelationId,
            Ms4StockEventType resolvedEventType
    ) {
        if (envelopeEventId == null) {
            throw new IllegalArgumentException(
                    "El eventId del envelope es obligatorio."
            );
        }

        if (resolvedEventType == null) {
            throw new IllegalArgumentException(
                    "El eventType resuelto del envelope es obligatorio."
            );
        }

        if (
                hasText(eventId)
                        && !envelopeEventId
                        .toString()
                        .equalsIgnoreCase(
                                eventId.trim()
                        )
        ) {
            throw new IllegalArgumentException(
                    "El eventId del payload no coincide con el eventId del envelope."
            );
        }

        if (
                eventType != null
                        && eventType != resolvedEventType
        ) {
            throw new IllegalArgumentException(
                    "El eventType del payload no coincide con el eventType del envelope."
            );
        }

        if (
                occurredAt != null
                        && envelopeOccurredAt != null
                        && !occurredAt.equals(
                        envelopeOccurredAt
                )
        ) {
            throw new IllegalArgumentException(
                    "El occurredAt del payload no coincide con el occurredAt del envelope."
            );
        }

        return Ms4StockCommandPayload.builder()
                .eventId(
                        envelopeEventId.toString()
                )
                .idempotencyKey(
                        clean(idempotencyKey)
                )
                .eventType(
                        resolvedEventType
                )
                .sku(sku)
                .almacen(almacen)
                .referenciaTipo(
                        referenciaTipo
                )
                .referenciaIdExterno(
                        safeReferenciaIdExterno()
                )
                .cantidad(cantidad)
                .codigoReserva(
                        safeCodigoReserva()
                )
                .actorIdUsuarioMs1(
                        actorIdUsuarioMs1
                )
                .actorIdEmpleadoMs2(
                        actorIdEmpleadoMs2
                )
                .actorRol(actorRol)
                .occurredAt(
                        envelopeOccurredAt == null
                                ? occurredAt
                                : envelopeOccurredAt
                )
                .expiresAt(expiresAt)
                .motivo(
                        clean(motivo)
                )
                .requestId(
                        firstText(
                                envelopeRequestId,
                                requestId
                        )
                )
                .correlationId(
                        firstText(
                                envelopeCorrelationId,
                                correlationId
                        )
                )
                .metadataJson(
                        metadataJson
                )
                .build();
    }

    public String safeEventId() {
        return clean(eventId);
    }

    public String safeIdempotencyKey() {
        /*
         * La idempotencyKey es parte obligatoria del contrato.
         *
         * No debe usarse eventId como fallback porque ocultaría un
         * productor defectuoso y mezclaría dos conceptos distintos.
         */
        return clean(idempotencyKey);
    }

    public String safeReferenciaIdExterno() {
        return clean(
                referenciaIdExterno
        );
    }

    public String safeCodigoReserva() {
        return clean(
                codigoReserva
        );
    }

    private String firstText(
            String first,
            String second
    ) {
        if (hasText(first)) {
            return first.trim();
        }

        return clean(second);
    }

    private String clean(
            String value
    ) {
        return hasText(value)
                ? value.trim()
                : null;
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}