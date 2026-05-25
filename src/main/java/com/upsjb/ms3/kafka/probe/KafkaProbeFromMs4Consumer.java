package com.upsjb.ms3.kafka.probe;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class KafkaProbeFromMs4Consumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProbeFromMs4Consumer.class);

    private final ObjectMapper objectMapper;
    private final KafkaProbeProperties properties;
    private final KafkaProbePublisher publisher;
    private final KafkaProbeRegistry registry;

    public KafkaProbeFromMs4Consumer(
            ObjectMapper objectMapper,
            KafkaProbeProperties properties,
            KafkaProbePublisher publisher,
            KafkaProbeRegistry registry
    ) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.publisher = publisher;
        this.registry = registry;
    }

    @KafkaListener(
            topics = "${app.kafka.probe.topics.ms4-to-ms3:dev.ms4.ms3.probe.v1}",
            groupId = "${app.kafka.probe.consumer-group:ms3-probe-consumer}"
    )
    public void consumeProbeFromMs4(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment
    ) {
        try {
            if (record == null || record.value() == null || record.value().isBlank()) {
                log.warn("[KAFKA-PROBE][MS3] Probe vacío recibido desde MS4.");
                acknowledgment.acknowledge();
                return;
            }

            KafkaProbePayload probe = objectMapper.readValue(
                    record.value(),
                    KafkaProbePayload.class
            );

            if (!probe.valid()) {
                log.warn(
                        "[KAFKA-PROBE][MS3] Probe inválido recibido desde MS4. topic={}, partition={}, offset={}, key={}, payload={}",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.key(),
                        record.value()
                );

                acknowledgment.acknowledge();
                return;
            }

            registry.markReceived(
                    probe.probeId(),
                    "MS4_TO_MS3",
                    record.topic(),
                    record.key(),
                    "MS3 received probe from MS4."
            );

            log.info(
                    "[KAFKA-PROBE][MS3] Probe recibido desde MS4. probeId={}, topic={}, partition={}, offset={}, key={}",
                    probe.probeId(),
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key()
            );

            KafkaProbeAckPayload ack = KafkaProbeAckPayload.ackToMs4(
                    probe,
                    properties.getServiceName(),
                    probe.sourceService(),
                    record.topic(),
                    record.key()
            );

            String ackTopic = properties.ms3ToMs4AckTopic();
            String ackKey = "probe-ack:" + probe.probeId();

            RecordMetadata metadata = publisher.publishAck(ack, ackTopic, ackKey);

            log.info(
                    "[KAFKA-PROBE][MS3] ACK enviado hacia MS4. probeId={}, topic={}, key={}, partition={}, offset={}",
                    probe.probeId(),
                    ackTopic,
                    ackKey,
                    metadata == null ? null : metadata.partition(),
                    metadata == null ? null : metadata.offset()
            );

            acknowledgment.acknowledge();
        } catch (Exception ex) {
            log.error(
                    "[KAFKA-PROBE][MS3] Error consumiendo probe desde MS4. topic={}, partition={}, offset={}, key={}",
                    record == null ? null : record.topic(),
                    record == null ? null : record.partition(),
                    record == null ? null : record.offset(),
                    record == null ? null : record.key(),
                    ex
            );

            /*
             * Como es una prueba técnica sin persistencia, se confirma el offset
             * para evitar reintentos infinitos por payloads corruptos.
             */
            acknowledgment.acknowledge();
        }
    }
}