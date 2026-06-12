package com.upsjb.ms3.kafka.consumer;

import com.upsjb.ms3.kafka.consumer.Ms2EmpleadoSnapshotHandler.Ms2EmpleadoSnapshotResult;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class Ms2EmpleadoSnapshotConsumerMetrics {

    private final AtomicLong received =
            new AtomicLong();

    private final AtomicLong handled =
            new AtomicLong();

    private final AtomicLong processed =
            new AtomicLong();

    private final AtomicLong created =
            new AtomicLong();

    private final AtomicLong updated =
            new AtomicLong();

    private final AtomicLong duplicated =
            new AtomicLong();

    private final AtomicLong stale =
            new AtomicLong();

    private final AtomicLong functionalProbesIgnored =
            new AtomicLong();

    private final AtomicLong failures =
            new AtomicLong();

    public void recordReceived() {
        received.incrementAndGet();
    }

    public void recordResult(
            Ms2EmpleadoSnapshotResult result
    ) {
        if (result == null) {
            return;
        }

        handled.incrementAndGet();

        if (result.processed()) {
            processed.incrementAndGet();

            if (result.created()) {
                created.incrementAndGet();
            } else {
                updated.incrementAndGet();
            }
        }

        if (result.duplicated()) {
            duplicated.incrementAndGet();
        }

        if (result.stale()) {
            stale.incrementAndGet();
        }
    }

    public void recordFunctionalProbeIgnored() {
        functionalProbesIgnored.incrementAndGet();
    }

    public void recordFailure() {
        failures.incrementAndGet();
    }

    public Snapshot snapshot() {
        return new Snapshot(
                received.get(),
                handled.get(),
                processed.get(),
                created.get(),
                updated.get(),
                duplicated.get(),
                stale.get(),
                functionalProbesIgnored.get(),
                failures.get()
        );
    }

    public record Snapshot(
            long received,
            long handled,
            long processed,
            long created,
            long updated,
            long duplicated,
            long stale,
            long functionalProbesIgnored,
            long failures
    ) {
    }
}