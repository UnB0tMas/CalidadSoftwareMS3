// ruta: src/main/java/com/upsjb/ms3/kafka/outbox/OutboxRetryPolicy.java
package com.upsjb.ms3.kafka.outbox;

import com.upsjb.ms3.config.OutboxProperties;
import com.upsjb.ms3.domain.entity.EventoDominioOutbox;
import com.upsjb.ms3.domain.enums.EstadoPublicacionEvento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxRetryPolicy {

    private final OutboxProperties properties;

    public boolean canPublish(EventoDominioOutbox event) {
        if (event == null || !Boolean.TRUE.equals(event.getEstado())) {
            return false;
        }

        if (event.getEstadoPublicacion() == null) {
            return false;
        }

        boolean validState = event.getEstadoPublicacion() == EstadoPublicacionEvento.PENDIENTE
                || event.getEstadoPublicacion() == EstadoPublicacionEvento.ERROR;

        return validState && properties.canRetry(event.getIntentosPublicacion());
    }

    public boolean shouldMarkPermanentError(EventoDominioOutbox event) {
        if (event == null) {
            return true;
        }

        return !properties.canRetry(event.getIntentosPublicacion());
    }

    public String permanentErrorMessage() {
        return "El evento superó el máximo de intentos configurado para publicación Kafka.";
    }
}