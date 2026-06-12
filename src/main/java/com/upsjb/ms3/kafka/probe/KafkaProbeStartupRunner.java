package com.upsjb.ms3.kafka.probe;

import com.upsjb.ms3.config.KafkaTopicProperties;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaProbeStartupRunner {

    private static final Logger log =
            LoggerFactory.getLogger(
                    KafkaProbeStartupRunner.class
            );

    private static final String DIRECTION =
            "MS3_TO_MS4_FUNCTIONAL";

    private final KafkaProbeProperties properties;
    private final KafkaTopicProperties topicProperties;
    private final KafkaProbeRegistry registry;
    private final KafkaFunctionalStockSnapshotProbeService
            functionalProbeService;

    private final AtomicBoolean startupExecuted =
            new AtomicBoolean(false);

    public KafkaProbeStartupRunner(
            KafkaProbeProperties properties,
            KafkaTopicProperties topicProperties,
            KafkaProbeRegistry registry,
            KafkaFunctionalStockSnapshotProbeService functionalProbeService
    ) {
        this.properties = properties;
        this.topicProperties = topicProperties;
        this.registry = registry;
        this.functionalProbeService = functionalProbeService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (
                !properties.isEnabled()
                        || !properties.isRunOnStartup()
        ) {
            log.info(
                    "[KAFKA-FUNCTIONAL-E2E][MS3] Prueba deshabilitada por configuración."
            );
            return;
        }

        if (!startupExecuted.compareAndSet(false, true)) {
            return;
        }

        sleep(
                properties.safeInitialDelayMs()
        );

        String probeId =
                newProbeId();

        try {
            runProbe(probeId);
        } catch (RuntimeException ex) {
            printFailure(
                    probeId,
                    ex
            );

            if (properties.isFailOnTimeout()) {
                throw ex;
            }
        }
    }

    public String runManualProbe() {
        String probeId =
                newProbeId();

        runProbe(probeId);

        return probeId;
    }

    private void runProbe(
            String probeId
    ) {
        String expectedTopic =
                topicProperties
                        .resolveStockSnapshotTopic();

        registry.markPending(
                probeId,
                DIRECTION,
                expectedTopic,
                "PENDING:" + probeId,
                1
        );

        KafkaFunctionalStockSnapshotProbeService.Publication
                publication;

        try {
            publication =
                    functionalProbeService.publish(
                            probeId
                    );
        } catch (RuntimeException ex) {
            registry.markFailed(
                    probeId,
                    DIRECTION,
                    expectedTopic,
                    "PENDING:" + probeId,
                    1,
                    ex.getMessage()
            );

            throw ex;
        }

        if (!registry.isAcked(probeId)) {
            registry.markPending(
                    probeId,
                    DIRECTION,
                    publication.topic(),
                    publication.eventKey(),
                    1
            );
        }

        log.info(
                "[KAFKA-FUNCTIONAL-E2E][MS3] Snapshot real de stock enviado. probeId={}, eventId={}, idStockMs3={}, idSkuMs3={}, idAlmacenMs3={}, stockDisponible={}, topic={}, key={}, partition={}, offset={}",
                probeId,
                publication.eventId(),
                publication.idStockMs3(),
                publication.idSkuMs3(),
                publication.idAlmacenMs3(),
                publication.stockDisponible(),
                publication.topic(),
                publication.eventKey(),
                publication.partition(),
                publication.offset()
        );

        awaitAck(
                probeId,
                publication
        );

        printSuccess(
                publication
        );
    }

    private void awaitAck(
            String probeId,
            KafkaFunctionalStockSnapshotProbeService.Publication
                    publication
    ) {
        for (
                int attempt = 1;
                attempt <= properties.safeMaxAttempts();
                attempt++
        ) {
            if (registry.isAcked(probeId)) {
                return;
            }

            log.info(
                    "[KAFKA-FUNCTIONAL-E2E][MS3] Esperando confirmación funcional de MS4. probeId={}, attempt={}/{}",
                    probeId,
                    attempt,
                    properties.safeMaxAttempts()
            );

            sleep(
                    properties.safeRetryDelayMs()
            );
        }

        String message =
                "MS4 no confirmó el procesamiento funcional del snapshot "
                        + publication.eventId();

        registry.markFailed(
                probeId,
                DIRECTION,
                publication.topic(),
                publication.eventKey(),
                properties.safeMaxAttempts(),
                message
        );

        throw new IllegalStateException(
                message
        );
    }

    private void printSuccess(
            KafkaFunctionalStockSnapshotProbeService.Publication
                    publication
    ) {
        log.info(
                """
                
                ========================================================================
                [KAFKA-FUNCTIONAL-E2E][MS3] RESULTADO=APROBADO
                probeId={}
                eventId={}
                flujo=MS3 -> OUTBOX REAL -> KAFKA -> MS4
                topic={}
                eventKey={}
                idStockMs3={}
                idSkuMs3={}
                idAlmacenMs3={}
                contratoStockReal=OK
                outboxCreadoYPublicado=OK
                consumoRealMS4=OK
                persistenciaSnapshotMS4=OK
                idempotenciaMS4=OK
                rollbackMS3=OK
                rollbackMS4=OK
                residuosBaseDatos=NINGUNO
                listoContextoReal=true
                ========================================================================
                """,
                publication.probeId(),
                publication.eventId(),
                publication.topic(),
                publication.eventKey(),
                publication.idStockMs3(),
                publication.idSkuMs3(),
                publication.idAlmacenMs3()
        );
    }

    private void printFailure(
            String probeId,
            RuntimeException ex
    ) {
        log.error(
                """
                
                ========================================================================
                [KAFKA-FUNCTIONAL-E2E][MS3] RESULTADO=FALLIDO
                probeId={}
                flujo=MS3 -> MS4
                listoContextoReal=false
                error={}
                Se aplicó rollback a toda escritura funcional de la prueba.
                ========================================================================
                """,
                probeId,
                ex.getMessage(),
                ex
        );
    }

    private String newProbeId() {
        String timestamp =
                DateTimeFormatter.ISO_INSTANT
                        .format(
                                Instant.now()
                        )
                        .replace(":", "")
                        .replace(".", "")
                        .replace("-", "");

        return "MS3-"
                + timestamp
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(
                        0,
                        8
                );
    }

    private void sleep(
            long millis
    ) {
        if (millis <= 0) {
            return;
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread()
                    .interrupt();

            throw new IllegalStateException(
                    "La prueba funcional Kafka MS3 fue interrumpida.",
                    ex
            );
        }
    }
}