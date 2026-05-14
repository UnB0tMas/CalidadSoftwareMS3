// ruta: src/main/java/com/upsjb/ms3/kafka/event/Ms4StockCommandPayload.java
package com.upsjb.ms3.kafka.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.upsjb.ms3.domain.enums.Ms4StockEventType;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import java.time.LocalDateTime;
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

    public String safeEventId() {
        return hasText(eventId) ? eventId.trim() : null;
    }

    public String safeIdempotencyKey() {
        return hasText(idempotencyKey) ? idempotencyKey.trim() : safeEventId();
    }

    public String safeReferenciaIdExterno() {
        return hasText(referenciaIdExterno) ? referenciaIdExterno.trim() : null;
    }

    public String safeCodigoReserva() {
        return hasText(codigoReserva) ? codigoReserva.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}