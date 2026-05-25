package com.upsjb.ms3.kafka.probe;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class KafkaProbeRegistry {

    private final Map<String, KafkaProbeRecord> records = new ConcurrentHashMap<>();

    public KafkaProbeRecord markPending(
            String probeId,
            String direction,
            String topic,
            String key,
            int attempt
    ) {
        KafkaProbeRecord record = new KafkaProbeRecord(
                probeId,
                direction,
                topic,
                key,
                KafkaProbeStatus.PENDING,
                attempt,
                Instant.now(),
                null,
                null
        );

        records.put(probeId, record);
        return record;
    }

    public KafkaProbeRecord markReceived(
            String probeId,
            String direction,
            String topic,
            String key,
            String message
    ) {
        KafkaProbeRecord record = new KafkaProbeRecord(
                probeId,
                direction,
                topic,
                key,
                KafkaProbeStatus.RECEIVED,
                1,
                Instant.now(),
                Instant.now(),
                message
        );

        records.put(probeId, record);
        return record;
    }

    public KafkaProbeRecord markAcked(
            String probeId,
            String direction,
            String topic,
            String key,
            String message
    ) {
        KafkaProbeRecord previous = records.get(probeId);

        KafkaProbeRecord record = new KafkaProbeRecord(
                probeId,
                direction,
                topic,
                key,
                KafkaProbeStatus.ACKED,
                previous == null ? 1 : previous.attempt(),
                previous == null ? Instant.now() : previous.sentAt(),
                Instant.now(),
                message
        );

        records.put(probeId, record);
        return record;
    }

    public KafkaProbeRecord markFailed(
            String probeId,
            String direction,
            String topic,
            String key,
            int attempt,
            String error
    ) {
        KafkaProbeRecord previous = records.get(probeId);

        KafkaProbeRecord record = new KafkaProbeRecord(
                probeId,
                direction,
                topic,
                key,
                KafkaProbeStatus.FAILED,
                attempt,
                previous == null ? Instant.now() : previous.sentAt(),
                null,
                error
        );

        records.put(probeId, record);
        return record;
    }

    public boolean isAcked(String probeId) {
        KafkaProbeRecord record = records.get(probeId);
        return record != null && record.status() == KafkaProbeStatus.ACKED;
    }

    public List<KafkaProbeRecord> findAll() {
        return records.values()
                .stream()
                .sorted(Comparator.comparing(KafkaProbeRecord::sentAt).reversed())
                .toList();
    }

    public KafkaProbeRecord findByProbeId(String probeId) {
        return probeId == null ? null : records.get(probeId);
    }

    public record KafkaProbeRecord(
            String probeId,
            String direction,
            String topic,
            String key,
            KafkaProbeStatus status,
            int attempt,
            Instant sentAt,
            Instant ackAt,
            String message
    ) {
    }
}