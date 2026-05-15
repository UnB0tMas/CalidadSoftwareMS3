package com.upsjb.ms3.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "empleado_inventario_permiso_historial")
public class EmpleadoInventarioPermisoHistorial extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso_historial")
    private Long idPermisoHistorial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empleado_snapshot", nullable = false)
    private EmpleadoSnapshotMs2 empleadoSnapshot;

    @Column(name = "puede_crear_producto_basico", nullable = false)
    private Boolean puedeCrearProductoBasico = Boolean.FALSE;

    @Column(name = "puede_editar_producto_basico", nullable = false)
    private Boolean puedeEditarProductoBasico = Boolean.FALSE;

    @Column(name = "puede_registrar_entrada", nullable = false)
    private Boolean puedeRegistrarEntrada = Boolean.FALSE;

    @Column(name = "puede_registrar_salida", nullable = false)
    private Boolean puedeRegistrarSalida = Boolean.FALSE;

    @Column(name = "puede_registrar_ajuste", nullable = false)
    private Boolean puedeRegistrarAjuste = Boolean.FALSE;

    @Column(name = "puede_consultar_kardex", nullable = false)
    private Boolean puedeConsultarKardex = Boolean.FALSE;

    @Column(name = "puede_gestionar_imagenes", nullable = false)
    private Boolean puedeGestionarImagenes = Boolean.FALSE;

    @Column(name = "puede_actualizar_atributos", nullable = false)
    private Boolean puedeActualizarAtributos = Boolean.FALSE;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "vigente", nullable = false)
    private Boolean vigente = Boolean.TRUE;

    @Column(name = "otorgado_por_id_usuario_ms1", nullable = false)
    private Long otorgadoPorIdUsuarioMs1;

    @Column(name = "revocado_por_id_usuario_ms1")
    private Long revocadoPorIdUsuarioMs1;

    @Column(name = "motivo", nullable = false, length = 500, columnDefinition = "nvarchar(500)")
    private String motivo;
}