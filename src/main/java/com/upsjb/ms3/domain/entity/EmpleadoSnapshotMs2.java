package com.upsjb.ms3.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "empleado_snapshot_ms2")
public class EmpleadoSnapshotMs2 extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado_snapshot")
    private Long idEmpleadoSnapshot;

    @Column(name = "id_empleado_ms2", nullable = false)
    private Long idEmpleadoMs2;

    @Column(name = "id_usuario_ms1", nullable = false)
    private Long idUsuarioMs1;

    @Column(name = "codigo_empleado", nullable = false, length = 50)
    private String codigoEmpleado;

    @Column(name = "nombres_completos", nullable = false, length = 250, columnDefinition = "nvarchar(250)")
    private String nombresCompletos;

    @Column(name = "area_codigo", length = 50)
    private String areaCodigo;

    @Column(name = "area_nombre", length = 120, columnDefinition = "nvarchar(120)")
    private String areaNombre;

    @Column(name = "empleado_activo", nullable = false)
    private Boolean empleadoActivo = Boolean.TRUE;

    @Column(name = "snapshot_version")
    private Long snapshotVersion;

    @Column(name = "snapshot_at", nullable = false)
    private LocalDateTime snapshotAt;
}