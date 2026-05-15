package com.upsjb.ms3.domain.entity;

import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.EstadoPublicacionEvento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "evento_dominio_outbox")
public class EventoDominioOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long idEvento;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregate_type", nullable = false, length = 80)
    private AggregateType aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(name = "topic", nullable = false, length = 200)
    private String topic;

    @Column(name = "event_key", nullable = false, length = 200)
    private String eventKey;

    @Column(name = "payload_json", nullable = false, columnDefinition = "nvarchar(max)")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_publicacion", nullable = false, length = 30)
    private EstadoPublicacionEvento estadoPublicacion = EstadoPublicacionEvento.PENDIENTE;

    @Column(name = "intentos_publicacion", nullable = false)
    private Integer intentosPublicacion = 0;

    @Column(name = "error_publicacion", columnDefinition = "nvarchar(max)")
    private String errorPublicacion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "locked_by", length = 100)
    private String lockedBy;

    @Column(name = "estado", nullable = false)
    private Boolean estado = Boolean.TRUE;

    @PrePersist
    private void onCreate() {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (estadoPublicacion == null) {
            estadoPublicacion = EstadoPublicacionEvento.PENDIENTE;
        }
        if (intentosPublicacion == null) {
            intentosPublicacion = 0;
        }
        if (estado == null) {
            estado = Boolean.TRUE;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public boolean isPendiente() {
        return EstadoPublicacionEvento.PENDIENTE.equals(estadoPublicacion);
    }

    public boolean isPublicado() {
        return EstadoPublicacionEvento.PUBLICADO.equals(estadoPublicacion);
    }

    public boolean isError() {
        return EstadoPublicacionEvento.ERROR.equals(estadoPublicacion);
    }
}