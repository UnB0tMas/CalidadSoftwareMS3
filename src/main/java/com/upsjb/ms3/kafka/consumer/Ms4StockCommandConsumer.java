// ruta: src/main/java/com/upsjb/ms3/kafka/consumer/Ms4StockCommandConsumer.java
package com.upsjb.ms3.kafka.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms3.config.AppPropertiesConfig;
import com.upsjb.ms3.dto.ms4.response.Ms4StockSyncResultDto;
import com.upsjb.ms3.kafka.event.DomainEventEnvelope;
import com.upsjb.ms3.kafka.event.Ms4StockCommandEvent;
import com.upsjb.ms3.kafka.event.Ms4StockCommandPayload;
import com.upsjb.ms3.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_KEY;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class Ms4StockCommandConsumer {

    private final ObjectMapper objectMapper;
    private final Ms4StockCommandHandler handler;
    private final KafkaConsumerErrorHandler errorHandler;
    private final AppPropertiesConfig appPropertiesConfig;

    @KafkaListener(
            topics = "${app.kafka.topics.ms4-stock-command}",
            groupId = "${spring.kafka.consumer.group-id:ms3-stock-command-consumer}"
    )
    public void consume(
            String rawMessage,
            @Header(name = RECEIVED_TOPIC, required = false) String topic,
            @Header(name = RECEIVED_KEY, required = false) String key,
            Acknowledgment acknowledgment
    ) {
        if (!appPropertiesConfig.getKafka().isEnabled()) {
            log.info("Kafka consumer MS4 omitido porque app.kafka.enabled=false.");
            acknowledgment.acknowledge();
            return;
        }

        try {
            Ms4StockCommandEvent event = deserialize(rawMessage);
            Ms4StockSyncResultDto result = handler.handle(event);

            log.info(
                    "Comando de stock MS4 procesado. eventId={}, type={}, processed={}, duplicated={}, code={}",
                    result.eventId(),
                    result.eventType(),
                    result.processed(),
                    result.duplicated(),
                    result.code()
            );

            acknowledgment.acknowledge();
        } catch (Exception ex) {
            boolean acknowledge = errorHandler.handle(topic, key, rawMessage, ex);

            if (acknowledge) {
                acknowledgment.acknowledge();
                return;
            }

            throw new IllegalStateException("Error técnico consumiendo comando de stock MS4.", ex);
        }
    }

    private Ms4StockCommandEvent deserialize(String rawMessage) throws Exception {
        if (!StringUtils.hasText(rawMessage)) {
            throw new ValidationException(
                    "KAFKA_MS4_STOCK_COMMAND_VACIO",
                    "El mensaje Kafka de stock enviado por MS4 está vacío."
            );
        }

        JsonNode root = objectMapper.readTree(rawMessage);

        if (root == null || root.isNull() || root.isMissingNode()) {
            throw new ValidationException(
                    "KAFKA_MS4_STOCK_COMMAND_INVALIDO",
                    "El mensaje Kafka de stock enviado por MS4 no tiene formato JSON válido."
            );
        }

        if (root.hasNonNull("envelope")) {
            return objectMapper.treeToValue(root, Ms4StockCommandEvent.class);
        }

        DomainEventEnvelope<Ms4StockCommandPayload> envelope = objectMapper.readValue(
                rawMessage,
                new TypeReference<DomainEventEnvelope<Ms4StockCommandPayload>>() {
                }
        );

        return new Ms4StockCommandEvent(envelope);
    }
}