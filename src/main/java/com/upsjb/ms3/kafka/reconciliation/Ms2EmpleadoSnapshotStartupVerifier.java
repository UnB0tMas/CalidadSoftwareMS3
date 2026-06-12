package com.upsjb.ms3.kafka.reconciliation;

import com.upsjb.ms3.config.EmpleadoSnapshotStartupVerificationProperties;
import com.upsjb.ms3.kafka.consumer.Ms2EmpleadoSnapshotConsumerMetrics;
import com.upsjb.ms3.kafka.consumer.Ms2EmpleadoSnapshotConsumerMetrics.Snapshot;
import com.upsjb.ms3.repository.EmpleadoSnapshotMs2Repository;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Ms2EmpleadoSnapshotStartupVerifier {

    private final EmpleadoSnapshotStartupVerificationProperties
            properties;

    private final KafkaAdmin kafkaAdmin;

    private final KafkaListenerEndpointRegistry
            listenerRegistry;

    private final Ms2EmpleadoSnapshotConsumerMetrics
            consumerMetrics;

    private final EmpleadoSnapshotMs2Repository
            empleadoSnapshotRepository;

    private final AtomicBoolean executed =
            new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void verifyOnStartup() {
        if (!properties.isEnabled()) {
            log.info(
                    "Verificación de reconciliación MS2 -> MS3 "
                            + "deshabilitada."
            );

            return;
        }

        if (!executed.compareAndSet(false, true)) {
            return;
        }

        sleepSafely(
                properties.getInitialDelay()
        );

        VerificationResult lastResult =
                null;

        for (
                int attempt = 1;
                attempt <= properties.getMaxAttempts();
                attempt++
        ) {
            lastResult =
                    verifyCurrentState(
                            attempt
                    );

            if (lastResult.successful()) {
                log.info(
                        "============================================================"
                );
                log.info(
                        "RECONCILIACIÓN DE EMPLEADOS MS2 -> MS3 VERIFICADA"
                );
                log.info(
                        "topic={}, partitions={}, cleanupPolicy={}, "
                                + "listenerRunning={}, eventosRecibidos={}, "
                                + "eventosProcesados={}, creados={}, actualizados={}, "
                                + "duplicados={}, antiguos={}, probesIgnorados={}, "
                                + "erroresConsumer={}, snapshotsLocales={}, attempt={}",
                        properties.getTopic(),
                        lastResult.topicStatus().partitions(),
                        lastResult.topicStatus().cleanupPolicy(),
                        lastResult.listenerRunning(),
                        lastResult.metrics().received(),
                        lastResult.metrics().processed(),
                        lastResult.metrics().created(),
                        lastResult.metrics().updated(),
                        lastResult.metrics().duplicated(),
                        lastResult.metrics().stale(),
                        lastResult.metrics().functionalProbesIgnored(),
                        lastResult.metrics().failures(),
                        lastResult.localSnapshots(),
                        attempt
                );
                log.info(
                        "El selector de permisos ya puede obtener los empleados "
                                + "desde el snapshot local de MS3."
                );
                log.info(
                        "============================================================"
                );

                return;
            }

            log.warn(
                    "Reconciliación MS2 -> MS3 todavía no verificada. "
                            + "attempt={}/{}, topicAvailable={}, topicCompacted={}, "
                            + "partitions={}, listenerRunning={}, handledEvents={}, "
                            + "consumerFailures={}, localSnapshots={}, detail={}",
                    attempt,
                    properties.getMaxAttempts(),
                    lastResult.topicStatus().available(),
                    lastResult.topicStatus().compacted(),
                    lastResult.topicStatus().partitions(),
                    lastResult.listenerRunning(),
                    lastResult.metrics().handled(),
                    lastResult.metrics().failures(),
                    lastResult.localSnapshots(),
                    lastResult.detail()
            );

            if (attempt < properties.getMaxAttempts()) {
                sleepSafely(
                        properties.getRetryDelay()
                );
            }
        }

        String message =
                buildFailureMessage(
                        lastResult
                );

        log.error(
                "============================================================"
        );
        log.error(
                "NO SE PUDO VERIFICAR LA RECONCILIACIÓN MS2 -> MS3"
        );
        log.error(
                message
        );
        log.error(
                "Revisa Kafka, el topic {}, el consumidor {} y los logs de MS2.",
                properties.getTopic(),
                properties.getListenerId()
        );
        log.error(
                "============================================================"
        );

        if (properties.isFailFast()) {
            throw new IllegalStateException(
                    message
            );
        }
    }

    private VerificationResult verifyCurrentState(
            int attempt
    ) {
        TopicStatus topicStatus =
                inspectTopic();

        MessageListenerContainer listenerContainer =
                listenerRegistry.getListenerContainer(
                        properties.getListenerId()
                );

        boolean listenerRunning =
                listenerContainer != null
                        && listenerContainer.isRunning();

        Snapshot metrics =
                consumerMetrics.snapshot();

        long localSnapshots =
                empleadoSnapshotRepository.count();

        boolean consumedEventsSatisfied =
                metrics.handled()
                        >= properties.getMinimumConsumedEvents();

        boolean snapshotsSatisfied =
                localSnapshots
                        >= properties.getMinimumSnapshots();

        boolean successful =
                topicStatus.available()
                        && topicStatus.compacted()
                        && listenerRunning
                        && consumedEventsSatisfied
                        && snapshotsSatisfied
                        && metrics.failures() == 0;

        String detail =
                successful
                        ? "OK"
                        : buildDetail(
                        topicStatus,
                        listenerRunning,
                        consumedEventsSatisfied,
                        snapshotsSatisfied,
                        metrics
                );

        return new VerificationResult(
                attempt,
                topicStatus,
                listenerRunning,
                metrics,
                localSnapshots,
                successful,
                detail
        );
    }

    private TopicStatus inspectTopic() {
        String topic =
                properties.getTopic();

        long timeoutMillis =
                safeTimeoutMillis(
                        properties.getAdminTimeout()
                );

        try (
                AdminClient adminClient =
                        AdminClient.create(
                                kafkaAdmin
                                        .getConfigurationProperties()
                        )
        ) {
            Map<String, TopicDescription> descriptions =
                    adminClient.describeTopics(
                                    Set.of(
                                            topic
                                    )
                            )
                            .allTopicNames()
                            .get(
                                    timeoutMillis,
                                    TimeUnit.MILLISECONDS
                            );

            TopicDescription description =
                    descriptions.get(
                            topic
                    );

            if (description == null) {
                return TopicStatus.unavailable(
                        "Kafka no devolvió la descripción del topic."
                );
            }

            ConfigResource resource =
                    new ConfigResource(
                            ConfigResource.Type.TOPIC,
                            topic
                    );

            Map<ConfigResource, Config> configs =
                    adminClient.describeConfigs(
                                    List.of(
                                            resource
                                    )
                            )
                            .all()
                            .get(
                                    timeoutMillis,
                                    TimeUnit.MILLISECONDS
                            );

            Config topicConfig =
                    configs.get(
                            resource
                    );

            ConfigEntry cleanupEntry =
                    topicConfig == null
                            ? null
                            : topicConfig.get(
                            TopicConfig.CLEANUP_POLICY_CONFIG
                    );

            String cleanupPolicy =
                    cleanupEntry == null
                            ? null
                            : cleanupEntry.value();

            boolean compacted =
                    containsPolicy(
                            cleanupPolicy,
                            TopicConfig.CLEANUP_POLICY_COMPACT
                    );

            return new TopicStatus(
                    true,
                    compacted,
                    description.partitions()
                            .size(),
                    cleanupPolicy,
                    null
            );
        } catch (Exception ex) {
            return TopicStatus.unavailable(
                    safeMessage(ex)
            );
        }
    }

    private boolean containsPolicy(
            String policies,
            String expectedPolicy
    ) {
        if (
                policies == null
                        || policies.isBlank()
                        || expectedPolicy == null
                        || expectedPolicy.isBlank()
        ) {
            return false;
        }

        for (String policy : policies.split(",")) {
            if (
                    expectedPolicy.equalsIgnoreCase(
                            policy.trim()
                    )
            ) {
                return true;
            }
        }

        return false;
    }

    private String buildDetail(
            TopicStatus topicStatus,
            boolean listenerRunning,
            boolean consumedEventsSatisfied,
            boolean snapshotsSatisfied,
            Snapshot metrics
    ) {
        StringBuilder detail =
                new StringBuilder();

        if (!topicStatus.available()) {
            appendDetail(
                    detail,
                    "topic no disponible: "
                            + topicStatus.error()
            );
        } else if (!topicStatus.compacted()) {
            appendDetail(
                    detail,
                    "cleanup.policy del topic no contiene compact"
            );
        }

        if (!listenerRunning) {
            appendDetail(
                    detail,
                    "listener Kafka de empleados no está ejecutándose"
            );
        }

        if (!consumedEventsSatisfied) {
            appendDetail(
                    detail,
                    "eventos reales consumidos="
                            + metrics.handled()
                            + ", mínimo esperado="
                            + properties.getMinimumConsumedEvents()
            );
        }

        if (!snapshotsSatisfied) {
            appendDetail(
                    detail,
                    "snapshots locales insuficientes"
            );
        }

        if (metrics.failures() > 0) {
            appendDetail(
                    detail,
                    "errores del consumidor="
                            + metrics.failures()
            );
        }

        return detail.isEmpty()
                ? "estado incompleto"
                : detail.toString();
    }

    private void appendDetail(
            StringBuilder detail,
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return;
        }

        if (!detail.isEmpty()) {
            detail.append(
                    "; "
            );
        }

        detail.append(
                value
        );
    }

    private String buildFailureMessage(
            VerificationResult result
    ) {
        if (result == null) {
            return "No se obtuvo información durante la verificación.";
        }

        return "La reconciliación no alcanzó el estado esperado. "
                + "topicAvailable="
                + result.topicStatus().available()
                + ", topicCompacted="
                + result.topicStatus().compacted()
                + ", partitions="
                + result.topicStatus().partitions()
                + ", listenerRunning="
                + result.listenerRunning()
                + ", handledEvents="
                + result.metrics().handled()
                + ", consumerFailures="
                + result.metrics().failures()
                + ", localSnapshots="
                + result.localSnapshots()
                + ", detail="
                + result.detail();
    }

    private void sleepSafely(
            Duration duration
    ) {
        if (
                duration == null
                        || duration.isNegative()
                        || duration.isZero()
        ) {
            return;
        }

        try {
            Thread.sleep(
                    duration.toMillis()
            );
        } catch (InterruptedException ex) {
            Thread.currentThread()
                    .interrupt();

            throw new IllegalStateException(
                    "La verificación de reconciliación fue interrumpida.",
                    ex
            );
        }
    }

    private long safeTimeoutMillis(
            Duration duration
    ) {
        if (
                duration == null
                        || duration.isNegative()
                        || duration.isZero()
        ) {
            return 5000L;
        }

        return Math.max(
                1L,
                duration.toMillis()
        );
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

    private record TopicStatus(
            boolean available,
            boolean compacted,
            int partitions,
            String cleanupPolicy,
            String error
    ) {

        private static TopicStatus unavailable(
                String error
        ) {
            return new TopicStatus(
                    false,
                    false,
                    0,
                    null,
                    error
            );
        }
    }

    private record VerificationResult(
            int attempt,
            TopicStatus topicStatus,
            boolean listenerRunning,
            Snapshot metrics,
            long localSnapshots,
            boolean successful,
            String detail
    ) {
    }
}