// ruta: src/main/java/com/upsjb/ms3/kafka/outbox/OutboxScheduler.java
package com.upsjb.ms3.kafka.outbox;

import com.upsjb.ms3.dto.outbox.response.OutboxPublishResultResponseDto;
import com.upsjb.ms3.service.contract.KafkaPublisherService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OutboxScheduler {

    private final KafkaPublisherService kafkaPublisherService;

    @Scheduled(fixedDelayString = "#{@outboxProperties.fixedDelay.toMillis()}")
    public void publishPendingEvents() {
        try {
            List<OutboxPublishResultResponseDto> results = kafkaPublisherService.publicarPendientesInterno();

            if (!results.isEmpty()) {
                long success = results.stream()
                        .filter(result -> Boolean.TRUE.equals(result.success()))
                        .count();

                long failed = results.stream()
                        .filter(result -> !Boolean.TRUE.equals(result.success()))
                        .count();

                log.info(
                        "Outbox Kafka batch procesado. total={}, publicados={}, fallidos={}",
                        results.size(),
                        success,
                        failed
                );
            }
        } catch (Exception ex) {
            log.error("Error técnico al ejecutar scheduler Outbox Kafka.", ex);
        }
    }
}