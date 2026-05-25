package com.upsjb.ms3.kafka.probe;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/ms3/kafka/probe")
public class KafkaProbeController {

    private final KafkaProbeRegistry registry;
    private final KafkaProbeStartupRunner runner;
    private final KafkaProbeProperties properties;

    public KafkaProbeController(
            KafkaProbeRegistry registry,
            KafkaProbeStartupRunner runner,
            KafkaProbeProperties properties
    ) {
        this.registry = registry;
        this.runner = runner;
        this.properties = properties;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        List<KafkaProbeRegistry.KafkaProbeRecord> records = registry.findAll();

        return ResponseEntity.ok(Map.of(
                "service", properties.getServiceName(),
                "targetMs4", properties.getTargetMs4(),
                "enabled", properties.isEnabled(),
                "runOnStartup", properties.isRunOnStartup(),
                "topics", Map.of(
                        "ms3ToMs4", properties.ms3ToMs4Topic(),
                        "ms4ToMs3Ack", properties.ms4ToMs3AckTopic(),
                        "ms4ToMs3", properties.ms4ToMs3Topic(),
                        "ms3ToMs4Ack", properties.ms3ToMs4AckTopic()
                ),
                "records", records
        ));
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> run() {
        String probeId = runner.runManualProbe();

        return ResponseEntity.accepted().body(Map.of(
                "message", "Kafka Probe MS3 ejecutado.",
                "probeId", probeId,
                "status", registry.findByProbeId(probeId)
        ));
    }
}