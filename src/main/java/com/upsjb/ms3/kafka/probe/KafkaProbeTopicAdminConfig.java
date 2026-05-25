package com.upsjb.ms3.kafka.probe;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaProbeTopicAdminConfig {

    private static final int LOCAL_PARTITIONS = 1;
    private static final int LOCAL_REPLICAS = 1;

    @Bean
    public NewTopic ms3ToMs4ProbeTopic(KafkaProbeProperties properties) {
        return topic(properties.ms3ToMs4Topic());
    }

    @Bean
    public NewTopic ms4ToMs3ProbeAckTopic(KafkaProbeProperties properties) {
        return topic(properties.ms4ToMs3AckTopic());
    }

    @Bean
    public NewTopic ms4ToMs3ProbeTopic(KafkaProbeProperties properties) {
        return topic(properties.ms4ToMs3Topic());
    }

    @Bean
    public NewTopic ms3ToMs4ProbeAckTopic(KafkaProbeProperties properties) {
        return topic(properties.ms3ToMs4AckTopic());
    }

    private NewTopic topic(String name) {
        return TopicBuilder
                .name(name)
                .partitions(LOCAL_PARTITIONS)
                .replicas(LOCAL_REPLICAS)
                .build();
    }
}