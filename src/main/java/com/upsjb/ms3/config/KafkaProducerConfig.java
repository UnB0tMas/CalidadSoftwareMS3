package com.upsjb.ms3.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

    private static final String DEFAULT_CLIENT_ID =
            "ms3-outbox-producer";

    private static final String DEFAULT_ACKS =
            "all";

    private static final String DEFAULT_COMPRESSION_TYPE =
            "none";

    private static final int DEFAULT_MAX_IN_FLIGHT =
            5;

    private static final int DEFAULT_DELIVERY_TIMEOUT_MS =
            120_000;

    private static final int DEFAULT_REQUEST_TIMEOUT_MS =
            30_000;

    private static final int DEFAULT_LINGER_MS =
            5;

    private static final int DEFAULT_BATCH_SIZE =
            32_768;

    private static final int DEFAULT_RETRY_BACKOFF_MS =
            1_000;

    private final KafkaProperties kafkaProperties;

    public KafkaProducerConfig(
            KafkaProperties kafkaProperties
    ) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> properties =
                new HashMap<>(
                        kafkaProperties.buildProducerProperties()
                );

        /*
         * Se usan valores predeterminados únicamente cuando no han
         * sido configurados en application.properties.
         *
         * Esto permite que propiedades como compression.type=none
         * sean respetadas y evita volver a forzar zstd desde Java.
         */
        properties.putIfAbsent(
                ProducerConfig.CLIENT_ID_CONFIG,
                DEFAULT_CLIENT_ID
        );

        properties.putIfAbsent(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        properties.putIfAbsent(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        properties.putIfAbsent(
                ProducerConfig.ACKS_CONFIG,
                DEFAULT_ACKS
        );

        properties.putIfAbsent(
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                true
        );

        properties.putIfAbsent(
                ProducerConfig.RETRIES_CONFIG,
                Integer.MAX_VALUE
        );

        properties.putIfAbsent(
                ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                DEFAULT_MAX_IN_FLIGHT
        );

        properties.putIfAbsent(
                ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG,
                DEFAULT_DELIVERY_TIMEOUT_MS
        );

        properties.putIfAbsent(
                ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                DEFAULT_REQUEST_TIMEOUT_MS
        );

        properties.putIfAbsent(
                ProducerConfig.LINGER_MS_CONFIG,
                DEFAULT_LINGER_MS
        );

        properties.putIfAbsent(
                ProducerConfig.BATCH_SIZE_CONFIG,
                DEFAULT_BATCH_SIZE
        );

        properties.putIfAbsent(
                ProducerConfig.RETRY_BACKOFF_MS_CONFIG,
                DEFAULT_RETRY_BACKOFF_MS
        );

        properties.putIfAbsent(
                ProducerConfig.COMPRESSION_TYPE_CONFIG,
                DEFAULT_COMPRESSION_TYPE
        );

        return new DefaultKafkaProducerFactory<>(
                properties
        );
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(
            ProducerFactory<String, String> producerFactory
    ) {
        return new KafkaTemplate<>(
                producerFactory
        );
    }
}