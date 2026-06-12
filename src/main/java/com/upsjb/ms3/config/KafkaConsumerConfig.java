package com.upsjb.ms3.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.upsjb.ms3.shared.exception.BusinessException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    private static final String DEFAULT_GROUP_ID =
            "ms3-stock-command-consumer";

    private static final long RETRY_INTERVAL_MILLIS =
            1_000L;

    private static final long RETRY_ATTEMPTS =
            2L;

    private static final int BUSINESS_MAX_POLL_RECORDS =
            25;

    private static final int PROBE_MAX_POLL_RECORDS =
            10;

    private final KafkaProperties kafkaProperties;
    private final KafkaTopicProperties kafkaTopicProperties;

    public KafkaConsumerConfig(
            KafkaProperties kafkaProperties,
            KafkaTopicProperties kafkaTopicProperties
    ) {
        this.kafkaProperties =
                kafkaProperties;

        this.kafkaTopicProperties =
                kafkaTopicProperties;
    }

    @Bean
    public ConsumerFactory<String, String>
    consumerFactory() {
        Map<String, Object> properties =
                buildBaseConsumerProperties();

        properties.putIfAbsent(
                ConsumerConfig.GROUP_ID_CONFIG,
                DEFAULT_GROUP_ID
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                BUSINESS_MAX_POLL_RECORDS
        );

        return new DefaultKafkaConsumerFactory<>(
                properties
        );
    }

    @Bean
    public DeadLetterPublishingRecoverer
    deadLetterPublishingRecoverer(
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(
                                        kafkaTopicProperties
                                                .resolveDeadLetterTopic(),
                                        -1
                                )
                );

        recoverer.setFailIfSendResultIsError(
                true
        );

        recoverer.setWaitForSendResultTimeout(
                Duration.ofSeconds(10)
        );

        recoverer.setAppendOriginalHeaders(
                true
        );

        recoverer.setStripPreviousExceptionHeaders(
                true
        );

        recoverer.setLogRecoveryRecord(
                true
        );

        return recoverer;
    }

    @Bean
    public DefaultErrorHandler kafkaCommonErrorHandler(
            DeadLetterPublishingRecoverer
                    deadLetterPublishingRecoverer
    ) {
        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(
                        deadLetterPublishingRecoverer,
                        new FixedBackOff(
                                RETRY_INTERVAL_MILLIS,
                                RETRY_ATTEMPTS
                        )
                );

        /*
         * Los errores contractuales no deben reintentarse.
         *
         * Los errores técnicos temporales, incluida una posible
         * indisponibilidad al publicar el ACK, sí se reintentan.
         */
        errorHandler.addNotRetryableExceptions(
                BusinessException.class,
                IllegalArgumentException.class,
                JsonProcessingException.class
        );

        errorHandler.setAckAfterHandle(
                true
        );

        errorHandler.setResetStateOnExceptionChange(
                true
        );

        errorHandler.setResetStateOnRecoveryFailure(
                true
        );

        return errorHandler;
    }

    /*
     * Factory funcional para los comandos de stock enviados
     * desde MS4 hacia MS3.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler kafkaCommonErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String>
                factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                consumerFactory
        );

        factory.setCommonErrorHandler(
                kafkaCommonErrorHandler
        );

        factory.setConcurrency(1);

        factory.getContainerProperties()
                .setAckMode(
                        ContainerProperties.AckMode.RECORD
                );

        return factory;
    }

    /*
     * Factory exclusiva para Kafka Probe.
     *
     * Cada listener del probe define su propio groupId y
     * clientIdPrefix mediante @KafkaListener.
     */
    @Bean(name = "kafkaProbeListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaProbeListenerContainerFactory(
            DefaultErrorHandler kafkaCommonErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String>
                factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                buildProbeConsumerFactory()
        );

        factory.setCommonErrorHandler(
                kafkaCommonErrorHandler
        );

        factory.setConcurrency(1);

        factory.getContainerProperties()
                .setAckMode(
                        ContainerProperties.AckMode.RECORD
                );

        return factory;
    }

    private ConsumerFactory<String, String>
    buildProbeConsumerFactory() {
        Map<String, Object> properties =
                buildBaseConsumerProperties();

        /*
         * Los listeners del probe definen sus propios identificadores.
         */
        properties.remove(
                ConsumerConfig.GROUP_ID_CONFIG
        );

        properties.remove(
                ConsumerConfig.CLIENT_ID_CONFIG
        );

        /*
         * Se mantiene earliest para ser compatible con el
         * comportamiento de MS2.
         *
         * De esta manera, si el probe se publica antes de que el
         * consumidor termine de iniciar, el mensaje no se pierde.
         */
        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                PROBE_MAX_POLL_RECORDS
        );

        return new DefaultKafkaConsumerFactory<>(
                properties
        );
    }

    private Map<String, Object>
    buildBaseConsumerProperties() {
        Map<String, Object> properties =
                new HashMap<>(
                        kafkaProperties
                                .buildConsumerProperties()
                );

        /*
         * Evita reutilizar un client-id global en diferentes
         * contenedores Kafka.
         */
        properties.remove(
                ConsumerConfig.CLIENT_ID_CONFIG
        );

        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        /*
         * Los topics son administrados por KafkaAdmin y por
         * kafka-topics-init. Un nombre incorrecto no debe crear
         * automáticamente otro topic.
         */
        properties.put(
                ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG,
                false
        );

        return properties;
    }
}