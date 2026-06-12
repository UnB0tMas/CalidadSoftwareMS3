package com.upsjb.ms3.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms3.dto.ms4.response.Ms4StockSyncResultDto;
import com.upsjb.ms3.kafka.event.Ms4StockCommandEvent;
import com.upsjb.ms3.kafka.event.Ms4StockCommandPayload;
import com.upsjb.ms3.kafka.probe.KafkaFunctionalStockCommandExecutor;
import com.upsjb.ms3.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class Ms4StockCommandConsumer {

    private final ObjectMapper objectMapper;
    private final Ms4StockCommandHandler handler;
    private final KafkaFunctionalStockCommandExecutor
            functionalProbeExecutor;

    @KafkaListener(
            topics = {
                    "${app.kafka.topics.ms4-stock-command}",
                    "${app.kafka.topics.ms4-stock-reconciliation}"
            },
            groupId = "${spring.kafka.consumer.group-id:ms3-stock-command-consumer}",
            autoStartup = "${app.kafka.enabled:true}"
    )
    public void consume(
            ConsumerRecord<String, String> record
    ) throws Exception {
        validateRecord(
                record
        );

        Ms4StockCommandEvent event =
                deserialize(
                        record.value()
                );

        validateKafkaKey(
                record.key(),
                event
        );

        Ms4StockSyncResultDto result;

        if (
                functionalProbeExecutor
                        .isFunctionalProbe(
                                event
                        )
        ) {
            result =
                    functionalProbeExecutor.execute(
                            record,
                            event,
                            handler
                    );
        } else {
            result =
                    handler.handle(
                            event
                    );
        }

        log.info(
                "Comando de stock MS4 procesado. topic={}, partition={}, offset={}, key={}, eventId={}, type={}, processed={}, duplicated={}, code={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                result.eventId(),
                result.eventType(),
                result.processed(),
                result.duplicated(),
                result.code()
        );
    }

    private void validateRecord(
            ConsumerRecord<String, String> record
    ) {
        if (record == null) {
            throw new ValidationException(
                    "KAFKA_MS4_STOCK_RECORD_NULO",
                    "El registro Kafka de stock enviado por MS4 es obligatorio."
            );
        }

        if (
                !StringUtils.hasText(
                        record.value()
                )
        ) {
            throw new ValidationException(
                    "KAFKA_MS4_STOCK_COMMAND_VACIO",
                    "El mensaje Kafka de stock enviado por MS4 está vacío."
            );
        }

        if (
                !StringUtils.hasText(
                        record.key()
                )
        ) {
            throw new ValidationException(
                    "KAFKA_MS4_STOCK_KEY_REQUERIDA",
                    "La key Kafka del comando de stock enviado por MS4 es obligatoria."
            );
        }
    }

    private Ms4StockCommandEvent deserialize(
            String rawMessage
    ) throws Exception {
        JsonNode root =
                objectMapper.readTree(
                        rawMessage
                );

        if (
                root == null
                        || root.isNull()
                        || root.isMissingNode()
                        || !root.isObject()
        ) {
            throw new ValidationException(
                    "KAFKA_MS4_STOCK_COMMAND_INVALIDO",
                    "El mensaje Kafka de stock enviado por MS4 no tiene formato JSON válido."
            );
        }

        if (!root.hasNonNull("envelope")) {
            throw new ValidationException(
                    "KAFKA_MS4_STOCK_COMMAND_FORMATO_INVALIDO",
                    "El contrato ms4.stock.command.v1 exige un objeto raíz con la propiedad envelope."
            );
        }

        return objectMapper.treeToValue(
                root,
                Ms4StockCommandEvent.class
        );
    }

    private void validateKafkaKey(
            String kafkaKey,
            Ms4StockCommandEvent event
    ) {
        if (
                event == null
                        || event.payload() == null
        ) {
            throw new ValidationException(
                    "KAFKA_MS4_STOCK_EVENTO_INVALIDO",
                    "El evento de stock recibido desde MS4 es obligatorio."
            );
        }

        Ms4StockCommandPayload payload =
                event.payload();

        String expectedKey =
                event.expectedKafkaKey();

        if (
                !expectedKey.equals(
                        kafkaKey.trim()
                )
        ) {
            throw new ValidationException(
                    "KAFKA_MS4_STOCK_KEY_INVALIDA",
                    "La key Kafka no coincide con el SKU y almacén informados en el payload. Se esperaba: "
                            + expectedKey
            );
        }

        if (
                !StringUtils.hasText(
                        payload.safeIdempotencyKey()
                )
        ) {
            throw new ValidationException(
                    "KAFKA_MS4_STOCK_IDEMPOTENCY_KEY_REQUERIDA",
                    "La idempotencyKey del comando de stock enviado por MS4 es obligatoria."
            );
        }
    }
}