package com.upsjb.ms3.domain.entity;

import com.upsjb.ms3.domain.enums.Moneda;
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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "precio_sku_historial")
public class PrecioSkuHistorial extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_precio_historial")
    private Long idPrecioHistorial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sku", nullable = false)
    private ProductoSku sku;

    @Column(name = "precio_venta", nullable = false, precision = 18, scale = 4)
    private BigDecimal precioVenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "moneda", nullable = false, length = 10)
    private Moneda moneda = Moneda.PEN;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "vigente", nullable = false)
    private Boolean vigente = Boolean.TRUE;

    @Column(name = "motivo", nullable = false, length = 500, columnDefinition = "nvarchar(500)")
    private String motivo;

    @Column(name = "creado_por_id_usuario_ms1", nullable = false)
    private Long creadoPorIdUsuarioMs1;
}