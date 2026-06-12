package com.upsjb.ms3.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "evento_empleado_ms2_consumido",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_evento_empleado_ms2_consumido_event_id",
                        columnNames = "event_id"
                )
        },
        indexes = {
                @Index(
                        name = "ix_evento_empleado_ms2_consumido_aggregate_id",
                        columnList = "aggregate_id"
                ),
                @Index(
                        name = "ix_evento_empleado_ms2_consumido_consumed_at",
                        columnList = "consumed_at"
                )
        }
)
public class EventoEmpleadoMs2Consumido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento_consumido")
    private Long idEventoConsumido;

    @Column(
            name = "event_id",
            nullable = false,
            updatable = false
    )
    private UUID eventId;

    @Column(
            name = "topic",
            nullable = false,
            length = 200,
            updatable = false
    )
    private String topic;

    @Column(
            name = "kafka_partition",
            nullable = false,
            updatable = false
    )
    private Integer partition;

    @Column(
            name = "kafka_offset",
            nullable = false,
            updatable = false
    )
    private Long kafkaOffset;

    @Column(
            name = "event_type",
            nullable = false,
            length = 120,
            updatable = false
    )
    private String eventType;

    @Column(
            name = "aggregate_id",
            nullable = false,
            updatable = false
    )
    private Long aggregateId;

    @Column(
            name = "occurred_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime occurredAt;

    @Column(
            name = "consumed_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime consumedAt;

    @PrePersist
    protected void onCreate() {
        if (consumedAt == null) {
            consumedAt = LocalDateTime.now();
        }
    }
}