// ruta: src/main/java/com/upsjb/ms3/kafka/outbox/OutboxLockService.java
package com.upsjb.ms3.kafka.outbox;

import com.upsjb.ms3.config.OutboxProperties;
import com.upsjb.ms3.domain.entity.EventoDominioOutbox;
import com.upsjb.ms3.domain.enums.EstadoPublicacionEvento;
import com.upsjb.ms3.mapper.EventoDominioOutboxMapper;
import com.upsjb.ms3.repository.EventoDominioOutboxRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxLockService {

    private final EventoDominioOutboxRepository repository;
    private final EventoDominioOutboxMapper mapper;
    private final OutboxProperties properties;
    private final OutboxRetryPolicy retryPolicy;

    @Transactional
    public List<EventoDominioOutbox> lockNextBatch() {
        LocalDateTime lockedAtBefore = LocalDateTime.now().minus(properties.getLockTimeout());

        List<EventoDominioOutbox> events = repository.findPublicablesForUpdate(
                List.of(EstadoPublicacionEvento.PENDIENTE, EstadoPublicacionEvento.ERROR),
                lockedAtBefore,
                PageRequest.of(0, properties.getBatchSize())
        );

        LocalDateTime lockedAt = LocalDateTime.now();

        return events.stream()
                .filter(retryPolicy::canPublish)
                .peek(event -> mapper.lock(event, properties.getPublisherId(), lockedAt))
                .map(repository::save)
                .toList();
    }

    @Transactional
    public void unlock(Long idEvento) {
        if (idEvento == null) {
            return;
        }

        repository.findActivoByIdForUpdate(idEvento).ifPresent(event -> {
            mapper.unlock(event);
            repository.save(event);
        });
    }
}