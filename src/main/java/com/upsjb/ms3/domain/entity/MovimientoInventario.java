package com.upsjb.ms3.domain.entity;

import com.upsjb.ms3.domain.enums.EstadoMovimientoInventario;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long idMovimiento;

    @Column(name = "codigo_movimiento", nullable = false, length = 100)
    private String codigoMovimiento;

    @Column(name = "codigo_generado", nullable = false)
    private Boolean codigoGenerado = Boolean.TRUE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sku", nullable = false)
    private ProductoSku sku;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_almacen", nullable = false)
    private Almacen almacen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra_detalle")
    private CompraInventarioDetalle compraDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva_stock")
    private ReservaStock reservaStock;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 50)
    private TipoMovimientoInventario tipoMovimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_movimiento", nullable = false, length = 80)
    private MotivoMovimientoInventario motivoMovimiento;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "costo_unitario", precision = 18, scale = 4)
    private BigDecimal costoUnitario;

    @Column(name = "costo_total", precision = 18, scale = 4)
    private BigDecimal costoTotal;

    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    @Column(name = "stock_nuevo", nullable = false)
    private Integer stockNuevo;

    @Column(name = "referencia_tipo", length = 50)
    private String referenciaTipo;

    @Column(name = "referencia_id_externo", length = 100)
    private String referenciaIdExterno;

    @Column(name = "observacion", length = 500, columnDefinition = "nvarchar(500)")
    private String observacion;

    @Column(name = "actor_id_usuario_ms1", nullable = false)
    private Long actorIdUsuarioMs1;

    @Column(name = "actor_id_empleado_ms2")
    private Long actorIdEmpleadoMs2;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_rol", nullable = false, length = 30)
    private RolSistema actorRol;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_movimiento", nullable = false, length = 30)
    private EstadoMovimientoInventario estadoMovimiento = EstadoMovimientoInventario.REGISTRADO;
}