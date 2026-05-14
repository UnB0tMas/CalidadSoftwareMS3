// ruta: src/main/java/com/upsjb/ms3/validator/EventoDominioOutboxValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.config.OutboxProperties;
import com.upsjb.ms3.domain.entity.EventoDominioOutbox;
import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.JsonUtil;
import com.upsjb.ms3.util.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventoDominioOutboxValidator {

    private final OutboxProperties outboxProperties;

    public void validateCreate(
            AggregateType aggregateType,
            String aggregateId,
            String eventType,
            String topic,
            String eventKey,
            String payloadJson
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (aggregateType == null) {
            errors.add("aggregateType", "El aggregate type es obligatorio.", "REQUIRED", null);
        }

        if (!StringNormalizer.hasText(aggregateId)) {
            errors.add("aggregateId", "El aggregate id es obligatorio.", "REQUIRED", aggregateId);
        }

        if (!StringNormalizer.hasText(eventType)) {
            errors.add("eventType", "El event type es obligatorio.", "REQUIRED", eventType);
        }

        if (!StringNormalizer.hasText(topic)) {
            errors.add("topic", "El topic Kafka es obligatorio.", "REQUIRED", topic);
        }

        if (!StringNormalizer.hasText(eventKey)) {
            errors.add("eventKey", "El event key es obligatorio.", "REQUIRED", eventKey);
        }

        if (!StringNormalizer.hasText(payloadJson)) {
            errors.add("payloadJson", "El payload JSON es obligatorio.", "REQUIRED", payloadJson);
        } else if (!JsonUtil.isValidJson(payloadJson)) {
            errors.add("payloadJson", "El payload no contiene un JSON válido.", "INVALID_JSON", null);
        }

        errors.throwIfAny("No se puede crear el evento outbox.");
    }

    public void validateCanPublish(EventoDominioOutbox event) {
        requireActive(event);

        if (!event.isPendiente() && !event.isError()) {
            throw new ConflictException(
                    "OUTBOX_EVENTO_NO_PUBLICABLE",
                    "Solo se pueden publicar eventos pendientes o en error."
            );
        }

        validateAttempts(event);
    }

    public void validateCanRetry(EventoDominioOutbox event) {
        requireActive(event);

        if (!event.isError()) {
            throw new ConflictException(
                    "OUTBOX_EVENTO_NO_REINTENTABLE",
                    "Solo se pueden reintentar eventos en estado ERROR."
            );
        }

        validateAttempts(event);
    }

    public void validateCanInspectPayload(boolean allowed) {
        if (!allowed) {
            throw new ConflictException(
                    "OUTBOX_PAYLOAD_NO_AUTORIZADO",
                    "No tiene autorización para visualizar el payload del evento."
            );
        }
    }

    public void requireActive(EventoDominioOutbox event) {
        if (event == null) {
            throw new NotFoundException(
                    "OUTBOX_EVENTO_NO_ENCONTRADO",
                    "Evento outbox no encontrado."
            );
        }

        if (!Boolean.TRUE.equals(event.getEstado())) {
            throw new NotFoundException(
                    "OUTBOX_EVENTO_INACTIVO",
                    "El evento outbox no está activo."
            );
        }
    }

    private void validateAttempts(EventoDominioOutbox event) {
        if (!outboxProperties.canRetry(event.getIntentosPublicacion())) {
            throw new ConflictException(
                    "OUTBOX_MAX_INTENTOS_SUPERADO",
                    "El evento superó el máximo de intentos de publicación configurado."
            );
        }
    }
}