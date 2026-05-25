package com.upsjb.ms3.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicAdminConfig {

    private static final int LOCAL_PARTITIONS = 3;
    private static final int LOCAL_REPLICAS = 1;

    @Bean
    public NewTopic ms3ProductoSnapshotTopic(KafkaTopicProperties properties) {
        return topic(properties.resolveProductoSnapshotTopic());
    }

    @Bean
    public NewTopic ms3PrecioSnapshotTopic(KafkaTopicProperties properties) {
        return topic(properties.resolvePrecioSnapshotTopic());
    }

    @Bean
    public NewTopic ms3PromocionSnapshotTopic(KafkaTopicProperties properties) {
        return topic(properties.resolvePromocionSnapshotTopic());
    }

    @Bean
    public NewTopic ms3StockSnapshotTopic(KafkaTopicProperties properties) {
        return topic(properties.resolveStockSnapshotTopic());
    }

    @Bean
    public NewTopic ms3MovimientoInventarioTopic(KafkaTopicProperties properties) {
        return topic(properties.resolveMovimientoInventarioTopic());
    }

    @Bean
    public NewTopic ms4StockCommandTopic(KafkaTopicProperties properties) {
        return topic(properties.resolveMs4StockCommandTopic());
    }

    @Bean
    public NewTopic ms4StockReconciliationTopic(KafkaTopicProperties properties) {
        return topic(properties.resolveMs4StockReconciliationTopic());
    }

    @Bean
    public NewTopic ms3DeadLetterTopic(KafkaTopicProperties properties) {
        return topic(properties.resolveDeadLetterTopic());
    }

    private NewTopic topic(String name) {
        return TopicBuilder
                .name(name)
                .partitions(LOCAL_PARTITIONS)
                .replicas(LOCAL_REPLICAS)
                .build();
    }
}