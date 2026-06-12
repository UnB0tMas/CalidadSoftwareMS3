package com.upsjb.ms3.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms3.kafka.consumer.Ms2EmpleadoSnapshotHandler.Ms2EmpleadoSnapshotResult;
import com.upsjb.ms3.kafka.event.Ms2EmpleadoSnapshotEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class Ms2EmpleadoSnapshotConsumer {

    public static final String LISTENER_ID =
            "ms2EmpleadoSnapshotConsumer";

    private final ObjectMapper objectMapper;
    private final Ms2EmpleadoSnapshotHandler handler;
    private final Ms2EmpleadoSnapshotConsumerMetrics metrics;

    @KafkaListener(
            id = LISTENER_ID,
            topics = "${app.kafka.topics.ms2-empleado-snapshot:"
                    + "ms2.empleado.snapshot.v1}",
            groupId = "${app.kafka.consumer-groups.ms2-empleado-snapshot:"
                    + "ms3-empleado-snapshot-consumer}",
            clientIdPrefix = "ms3-empleado-snapshot",
            containerFactory = "kafkaListenerContainerFactory",
            autoStartup = "${app.kafka.enabled:true}"
    )
    public void consume(
            ConsumerRecord<String, String> record
    ) throws JsonProcessingException {
        metrics.recordReceived();

        try {
            validateRecord(
                    record
            );

            Ms2EmpleadoSnapshotEvent event =
                    deserialize(
                            record
                    );

            /*
             * Se valida antes de decidir si es un probe.
             * Un mensaje sintético también debe cumplir el contrato.
             */
            event.validate();

            if (event.isFunctionalProbe()) {
                metrics.recordFunctionalProbeIgnored();

                log.info(
                        "Probe funcional de empleado MS2 validado e ignorado. "
                                + "No se persistirá como empleado real. "
                                + "topic={}, partition={}, offset={}, key={}, "
                                + "eventId={}, idEmpleadoMs2={}, codigoEmpleado={}",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.key(),
                        event.eventId(),
                        event.data().idEmpleado(),
                        event.data().codigoEmpleado()
                );

                return;
            }

            Ms2EmpleadoSnapshotResult result =
                    handler.handle(
                            record,
                            event
                    );

            metrics.recordResult(
                    result
            );

            log.info(
                    "Evento de empleado MS2 atendido. "
                            + "topic={}, partition={}, offset={}, key={}, "
                            + "eventId={}, idEmpleadoSnapshot={}, idEmpleadoMs2={}, "
                            + "processed={}, created={}, duplicated={}, stale={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    result.eventId(),
                    result.idEmpleadoSnapshot(),
                    result.idEmpleadoMs2(),
                    result.processed(),
                    result.created(),
                    result.duplicated(),
                    result.stale()
            );
        } catch (
                JsonProcessingException
                | RuntimeException ex
        ) {
            metrics.recordFailure();

            log.error(
                    "Error procesando evento de empleado MS2. "
                            + "topic={}, partition={}, offset={}, key={}, detail={}",
                    record == null
                            ? null
                            : record.topic(),
                    record == null
                            ? null
                            : record.partition(),
                    record == null
                            ? null
                            : record.offset(),
                    record == null
                            ? null
                            : record.key(),
                    safeMessage(ex),
                    ex
            );

            throw ex;
        }
    }

    private Ms2EmpleadoSnapshotEvent deserialize(
            ConsumerRecord<String, String> record
    ) throws JsonProcessingException {
        try {
            return objectMapper.readValue(
                    record.value(),
                    Ms2EmpleadoSnapshotEvent.class
            );
        } catch (JsonProcessingException ex) {
            log.error(
                    "No se pudo deserializar el evento de empleado MS2. "
                            + "topic={}, partition={}, offset={}, key={}, error={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    ex.getMessage()
            );

            throw ex;
        }
    }

    private void validateRecord(
            ConsumerRecord<String, String> record
    ) {
        if (record == null) {
            throw new IllegalArgumentException(
                    "El registro Kafka de empleado MS2 es obligatorio."
            );
        }

        if (!StringUtils.hasText(record.value())) {
            throw new IllegalArgumentException(
                    "El mensaje Kafka de empleado MS2 está vacío."
            );
        }
    }

    private String safeMessage(
            Throwable throwable
    ) {
        if (
                throwable == null
                        || throwable.getMessage() == null
                        || throwable.getMessage().isBlank()
        ) {
            return "error no especificado";
        }

        String message =
                throwable.getMessage()
                        .trim();

        return message.length() <= 500
                ? message
                : message.substring(
                0,
                500
        );
    }
}