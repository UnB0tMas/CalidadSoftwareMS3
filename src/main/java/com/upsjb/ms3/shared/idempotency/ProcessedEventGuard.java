package com.upsjb.ms3.shared.idempotency;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ProcessedEventGuard {

    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final Map<String, ProcessedEvent> processedEvents = new ConcurrentHashMap<>();
    private final Clock clock;

    public ProcessedEventGuard() {
        this.clock = Clock.systemUTC();
    }

    public DuplicateEventDecision check(String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return DuplicateEventDecision.REJECT_INVALID_KEY;
        }

        cleanupExpired();

        ProcessedEvent existing = processedEvents.get(idempotencyKey.trim());

        if (existing == null) {
            return DuplicateEventDecision.PROCESS;
        }

        return DuplicateEventDecision.ALREADY_PROCESSED;
    }

    public DuplicateEventDecision checkAndMarkProcessed(String idempotencyKey) {
        DuplicateEventDecision decision = check(idempotencyKey);

        if (decision.shouldProcess()) {
            markProcessed(idempotencyKey);
        }

        return decision;
    }

    public boolean isProcessed(String idempotencyKey) {
        return check(idempotencyKey).isDuplicate();
    }

    public void markProcessed(String idempotencyKey) {
        markProcessed(idempotencyKey, DEFAULT_TTL);
    }

    public void markProcessed(String idempotencyKey, Duration ttl) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new IllegalArgumentException("La clave idempotente es obligatoria.");
        }

        Duration resolvedTtl = ttl == null || ttl.isNegative() || ttl.isZero()
                ? DEFAULT_TTL
                : ttl;

        LocalDateTime now = LocalDateTime.now(clock);
        processedEvents.put(
                idempotencyKey.trim(),
                new ProcessedEvent(now, now.plus(resolvedTtl))
        );
    }

    public void forget(String idempotencyKey) {
        if (StringUtils.hasText(idempotencyKey)) {
            processedEvents.remove(idempotencyKey.trim());
        }
    }

    public int size() {
        cleanupExpired();
        return processedEvents.size();
    }

    private void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now(clock);
        processedEvents.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private record ProcessedEvent(
            LocalDateTime processedAt,
            LocalDateTime expiresAt
    ) {
    }
}