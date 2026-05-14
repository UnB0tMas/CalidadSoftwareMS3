// ruta: src/main/java/com/upsjb/ms3/kafka/outbox/OutboxEventPublisher.java
package com.upsjb.ms3.kafka.outbox;

import com.upsjb.ms3.domain.entity.EventoDominioOutbox;
import com.upsjb.ms3.kafka.producer.KafkaDomainEventPublisher;
import com.upsjb.ms3.mapper.EventoDominioOutboxMapper;
import com.upsjb.ms3.repository.EventoDominioOutboxRepository;
import com.upsjb.ms3.shared.exception.BusinessException;
import com.upsjb.ms3.validator.EventoDominioOutboxValidator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxLockService lockService;
    private final EventoDominioOutboxRepository repository;
    private final EventoDominioOutboxMapper mapper;
    private final EventoDominioOutboxValidator validator;
    private final OutboxRetryPolicy retryPolicy;
    private final KafkaDomainEventPublisher kafkaPublisher;

    public List<OutboxPublishResult> publishNextBatch() {
        List<EventoDominioOutbox> lockedEvents = lockService.lockNextBatch();
        List<OutboxPublishResult> results = new ArrayList<>();

        for (EventoDominioOutbox event : lockedEvents) {
            results.add(publishLocked(event.getIdEvento()));
        }

        return results;
    }

    @Transactional
    public OutboxPublishResult retry(Long idEvento) {
        EventoDominioOutbox event = repository.findActivoByIdForUpdate(idEvento)
                .orElse(null);

        validator.validateCanRetry(event);
        mapper.markPending(event);
        repository.save(event);

        return publishLocked(idEvento);
    }

    @Transactional
    public OutboxPublishResult publishLocked(Long idEvento) {
        EventoDominioOutbox event = repository.findActivoByIdForUpdate(idEvento)
                .orElse(null);

        validator.validateCanPublish(event);

        if (!retryPolicy.canPublish(event)) {
            mapper.markError(event, retryPolicy.permanentErrorMessage());
            repository.save(event);

            return OutboxPublishResult.failure(
                    event.getIdEvento(),
                    event.getEventId(),
                    event.getTopic(),
                    event.getEventKey(),
                    "OUTBOX_MAX_INTENTOS_SUPERADO",
                    retryPolicy.permanentErrorMessage()
            );
        }

        try {
            mapper.incrementAttempt(event);
            repository.saveAndFlush(event);

            OutboxPublishResult result = kafkaPublisher.publish(event);

            if (Boolean.TRUE.equals(result.success())) {
                mapper.markPublished(event, LocalDateTime.now());
            } else if (Boolean.TRUE.equals(result.skipped())) {
                mapper.unlock(event);
            } else {
                mapper.markError(event, result.message());
            }

            repository.save(event);
            return result;
        } catch (BusinessException ex) {
            mapper.markError(event, ex.getMessage());
            repository.save(event);

            return OutboxPublishResult.failure(
                    event.getIdEvento(),
                    event.getEventId(),
                    event.getTopic(),
                    event.getEventKey(),
                    ex.getCode(),
                    ex.getMessage()
            );
        } catch (Exception ex) {
            mapper.markError(event, ex.getMessage());
            repository.save(event);

            return OutboxPublishResult.failure(
                    event.getIdEvento(),
                    event.getEventId(),
                    event.getTopic(),
                    event.getEventKey(),
                    "EVENTO_KAFKA_PUBLICACION_FALLIDA",
                    "No se pudo publicar el evento en Kafka."
            );
        }
    }
}