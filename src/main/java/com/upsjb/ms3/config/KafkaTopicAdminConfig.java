package com.upsjb.ms3.config;

import java.time.Duration;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@ConditionalOnProperty(
        prefix = "app.kafka",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class KafkaTopicAdminConfig {

    private static final String ONE_DAY_MS =
            String.valueOf(
                    Duration.ofDays(1)
                            .toMillis()
            );

    private static final String THIRTY_DAYS_MS =
            String.valueOf(
                    Duration.ofDays(30)
                            .toMillis()
            );

    private static final String NINETY_DAYS_MS =
            String.valueOf(
                    Duration.ofDays(90)
                            .toMillis()
            );

    @Bean
    public KafkaAdmin kafkaAdmin(
            KafkaProperties properties
    ) {
        KafkaAdmin admin =
                new KafkaAdmin(
                        properties
                                .buildAdminProperties()
                );

        admin.setAutoCreate(true);
        admin.setModifyTopicConfigs(true);
        admin.setFatalIfBrokerNotAvailable(false);

        return admin;
    }

    /*
     * MS3 crea únicamente los topics de los que es productor.
     *
     * Los topics ms4.stock.command.v1 y
     * ms4.stock.reconciliation.v1 son propiedad de MS4.
     */

    @Bean
    public NewTopic ms3ProductoSnapshotTopic(
            KafkaTopicProperties properties
    ) {
        return compactedTopic(
                properties
                        .resolveProductoSnapshotTopic(),
                properties
        );
    }

    @Bean
    public NewTopic ms3PrecioSnapshotTopic(
            KafkaTopicProperties properties
    ) {
        return compactedTopic(
                properties
                        .resolvePrecioSnapshotTopic(),
                properties
        );
    }

    @Bean
    public NewTopic ms3PromocionSnapshotTopic(
            KafkaTopicProperties properties
    ) {
        return compactedTopic(
                properties
                        .resolvePromocionSnapshotTopic(),
                properties
        );
    }

    @Bean
    public NewTopic ms3StockSnapshotTopic(
            KafkaTopicProperties properties
    ) {
        return compactedTopic(
                properties
                        .resolveStockSnapshotTopic(),
                properties
        );
    }

    @Bean
    public NewTopic ms3MovimientoInventarioTopic(
            KafkaTopicProperties properties
    ) {
        return retainedTopic(
                properties
                        .resolveMovimientoInventarioTopic(),
                NINETY_DAYS_MS,
                properties
        );
    }

    @Bean
    public NewTopic ms3DeadLetterTopic(
            KafkaTopicProperties properties
    ) {
        return retainedTopic(
                properties
                        .resolveDeadLetterTopic(),
                THIRTY_DAYS_MS,
                properties
        );
    }

    private NewTopic compactedTopic(
            String name,
            KafkaTopicProperties properties
    ) {
        return TopicBuilder
                .name(name)
                .partitions(
                        properties
                                .resolvePartitions()
                )
                .replicas(
                        properties
                                .resolveReplicationFactor()
                )
                .compact()
                .config(
                        TopicConfig.MIN_CLEANABLE_DIRTY_RATIO_CONFIG,
                        "0.01"
                )
                .config(
                        TopicConfig.DELETE_RETENTION_MS_CONFIG,
                        ONE_DAY_MS
                )
                .build();
    }

    private NewTopic retainedTopic(
            String name,
            String retentionMs,
            KafkaTopicProperties properties
    ) {
        return TopicBuilder
                .name(name)
                .partitions(
                        properties
                                .resolvePartitions()
                )
                .replicas(
                        properties
                                .resolveReplicationFactor()
                )
                .config(
                        TopicConfig.CLEANUP_POLICY_CONFIG,
                        TopicConfig.CLEANUP_POLICY_DELETE
                )
                .config(
                        TopicConfig.RETENTION_MS_CONFIG,
                        retentionMs
                )
                .build();
    }
}