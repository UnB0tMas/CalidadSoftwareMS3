// ruta: src/main/java/com/upsjb/ms3/kafka/outbox/OutboxEventSerializer.java
package com.upsjb.ms3.kafka.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms3.kafka.event.DomainEventEnvelope;
import com.upsjb.ms3.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class OutboxEventSerializer {

    private final ObjectMapper objectMapper;

    public String toJson(Object value) {
        if (value == null) {
            throw new ValidationException(
                    "OUTBOX_PAYLOAD_NULL",
                    "El payload del evento outbox es obligatorio."
            );
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new ValidationException(
                    "OUTBOX_PAYLOAD_NO_SERIALIZABLE",
                    "No se pudo serializar el payload del evento outbox."
            );
        }
    }

    public String toEnvelopeJson(DomainEventEnvelope<?> envelope) {
        if (envelope == null) {
            throw new ValidationException(
                    "OUTBOX_ENVELOPE_NULL",
                    "El envelope del evento outbox es obligatorio."
            );
        }

        return toJson(envelope);
    }

    public <T> T fromJson(String json, Class<T> targetType) {
        if (!StringUtils.hasText(json)) {
            throw new ValidationException(
                    "OUTBOX_JSON_VACIO",
                    "El JSON del evento outbox es obligatorio."
            );
        }

        if (targetType == null) {
            throw new ValidationException(
                    "OUTBOX_TARGET_TYPE_NULL",
                    "El tipo destino para deserializar el evento outbox es obligatorio."
            );
        }

        try {
            return objectMapper.readValue(json, targetType);
        } catch (JsonProcessingException ex) {
            throw new ValidationException(
                    "OUTBOX_JSON_INVALIDO",
                    "El JSON del evento outbox no es válido."
            );
        }
    }
}