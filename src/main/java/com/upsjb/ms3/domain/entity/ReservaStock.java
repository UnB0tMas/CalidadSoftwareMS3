package com.upsjb.ms3.domain.entity;

import com.upsjb.ms3.domain.enums.EstadoReservaStock;
import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reserva_stock")
public class ReservaStock extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva_stock")
    private Long idReservaStock;

    @Column(name = "codigo_reserva", nullable = false, length = 80)
    private String codigoReserva;

    @Column(name = "codigo_generado", nullable = false)
    private Boolean codigoGenerado = Boolean.TRUE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sku", nullable = false)
    private ProductoSku sku;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_almacen", nullable = false)
    private Almacen almacen;

    @Enumerated(EnumType.STRING)
    @Column(name = "referencia_tipo", nullable = false, length = 40)
    private TipoReferenciaStock referenciaTipo;

    @Column(name = "referencia_id_externo", nullable = false, length = 100)
    private String referenciaIdExterno;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_reserva", nullable = false, length = 30)
    private EstadoReservaStock estadoReserva = EstadoReservaStock.RESERVADA;

    @Column(name = "reservado_por_id_usuario_ms1", nullable = false)
    private Long reservadoPorIdUsuarioMs1;

    @Column(name = "confirmado_por_id_usuario_ms1")
    private Long confirmadoPorIdUsuarioMs1;

    @Column(name = "liberado_por_id_usuario_ms1")
    private Long liberadoPorIdUsuarioMs1;

    @Column(name = "reservado_at", nullable = false)
    private LocalDateTime reservadoAt;

    @Column(name = "confirmado_at")
    private LocalDateTime confirmadoAt;

    @Column(name = "liberado_at")
    private LocalDateTime liberadoAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "motivo", length = 500, columnDefinition = "nvarchar(500)")
    private String motivo;
}