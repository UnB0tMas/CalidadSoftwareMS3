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

    private static final String DEFAULT_CLIENT_ID = "ms3-outbox-producer";

    private final KafkaProperties kafkaProperties;

    public KafkaProducerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>(
                kafkaProperties.buildProducerProperties()
        );

        props.putIfAbsent(ProducerConfig.CLIENT_ID_CONFIG, DEFAULT_CLIENT_ID);
        props.putIfAbsent(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.putIfAbsent(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        props.putIfAbsent(ProducerConfig.ACKS_CONFIG, "all");
        props.putIfAbsent(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.putIfAbsent(ProducerConfig.RETRIES_CONFIG, 3);
        props.putIfAbsent(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        props.putIfAbsent(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120_000);
        props.putIfAbsent(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30_000);
        props.putIfAbsent(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.putIfAbsent(ProducerConfig.BATCH_SIZE_CONFIG, 16_384);
        props.putIfAbsent(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1_000);
        props.putIfAbsent(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none");

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(
            ProducerFactory<String, String> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}