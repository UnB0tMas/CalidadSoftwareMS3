package com.upsjb.ms3.kafka.probe;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaProbeStartupRunner {

    private static final Logger log = LoggerFactory.getLogger(KafkaProbeStartupRunner.class);

    private final KafkaProbeProperties properties;
    private final KafkaProbePublisher publisher;
    private final KafkaProbeRegistry registry;

    public KafkaProbeStartupRunner(
            KafkaProbeProperties properties,
            KafkaProbePublisher publisher,
            KafkaProbeRegistry registry
    ) {
        this.properties = properties;
        this.publisher = publisher;
        this.registry = registry;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!properties.isEnabled() || !properties.isRunOnStartup()) {
            log.info("[KAFKA-PROBE][MS3] Probe deshabilitado por configuración.");
            return;
        }

        CompletableFuture.runAsync(this::runProbeSafely);
    }

    public String runManualProbe() {
        String probeId = newProbeId();
        runProbe(probeId);
        return probeId;
    }

    private void runProbeSafely() {
        sleep(properties.safeInitialDelayMs());

        String probeId = newProbeId();

        try {
            runProbe(probeId);
        } catch (Exception ex) {
            log.error("[KAFKA-PROBE][MS3] Error general ejecutando probeId={}", probeId, ex);

            if (properties.isFailOnTimeout()) {
                throw ex;
            }
        }
    }

    private void runProbe(String probeId) {
        String topic = properties.ms3ToMs4Topic();
        String key = "probe:" + probeId;

        for (int attempt = 1; attempt <= properties.safeMaxAttempts(); attempt++) {
            if (registry.isAcked(probeId)) {
                log.info("[KAFKA-PROBE][MS3] Probe ya confirmado. probeId={}", probeId);
                return;
            }

            try {
                KafkaProbePayload payload = KafkaProbePayload.ms3ToMs4(
                        probeId,
                        properties.getServiceName(),
                        properties.getTargetMs4()
                );

                registry.markPending(probeId, "MS3_TO_MS4", topic, key, attempt);

                RecordMetadata metadata = publisher.publishProbe(payload, topic, key);

                log.info(
                        "[KAFKA-PROBE][MS3] Probe enviado hacia MS4. probeId={}, attempt={}, topic={}, key={}, partition={}, offset={}",
                        probeId,
                        attempt,
                        topic,
                        key,
                        metadata == null ? null : metadata.partition(),
                        metadata == null ? null : metadata.offset()
                );

                sleep(properties.safeRetryDelayMs());

                if (registry.isAcked(probeId)) {
                    log.info("[KAFKA-PROBE][MS3] Resultado MS3_TO_MS4=ACKED. probeId={}", probeId);
                    return;
                }
            } catch (Exception ex) {
                registry.markFailed(
                        probeId,
                        "MS3_TO_MS4",
                        topic,
                        key,
                        attempt,
                        ex.getMessage()
                );

                log.warn(
                        "[KAFKA-PROBE][MS3] Falló intento de probe MS3_TO_MS4. probeId={}, attempt={}, error={}",
                        probeId,
                        attempt,
                        ex.getMessage()
                );

                sleep(properties.safeRetryDelayMs());
            }
        }

        String message = "No llegó ACK desde MS4 para probeId=" + probeId;
        registry.markFailed(
                probeId,
                "MS3_TO_MS4",
                topic,
                key,
                properties.safeMaxAttempts(),
                message
        );

        log.error("[KAFKA-PROBE][MS3] Resultado MS3_TO_MS4=FAILED. {}", message);

        if (properties.isFailOnTimeout()) {
            throw new IllegalStateException(message);
        }
    }

    private String newProbeId() {
        String timestamp = DateTimeFormatter.ISO_INSTANT
                .format(Instant.now())
                .replace(":", "")
                .replace(".", "")
                .replace("-", "");

        return "MS3-" + timestamp + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void sleep(long millis) {
        if (millis <= 0) {
            return;
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka Probe MS3 fue interrumpido.", ex);
        }
    }
}