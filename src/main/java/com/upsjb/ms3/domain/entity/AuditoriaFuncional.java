package com.upsjb.ms3.domain.entity;

import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.ResultadoAuditoria;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "auditoria_funcional")
public class AuditoriaFuncional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;

    @Column(name = "id_usuario_actor_ms1")
    private Long idUsuarioActorMs1;

    @Column(name = "id_empleado_actor_ms2")
    private Long idEmpleadoActorMs2;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_actor", length = 30)
    private RolSistema rolActor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 120)
    private TipoEventoAuditoria tipoEvento;

    @Enumerated(EnumType.STRING)
    @Column(name = "entidad", nullable = false, length = 120)
    private EntidadAuditada entidad;

    @Column(name = "id_registro_afectado", length = 100)
    private String idRegistroAfectado;

    @Column(name = "accion", nullable = false, length = 120)
    private String accion;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 30)
    private ResultadoAuditoria resultado;

    @Column(name = "descripcion", length = 1000, columnDefinition = "nvarchar(1000)")
    private String descripcion;

    @Column(name = "metadata_json", columnDefinition = "nvarchar(max)")
    private String metadataJson;

    @Column(name = "ip_address", length = 80)
    private String ipAddress;

    @Column(name = "user_agent", length = 500, columnDefinition = "nvarchar(500)")
    private String userAgent;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "event_at", nullable = false)
    private LocalDateTime eventAt;

    @PrePersist
    private void onCreate() {
        if (eventAt == null) {
            eventAt = LocalDateTime.now();
        }
    }
}