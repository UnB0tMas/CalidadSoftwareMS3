package com.upsjb.ms3.domain.entity;

import com.upsjb.ms3.domain.enums.EstadoCompraInventario;
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
@Table(name = "compra_inventario")
public class CompraInventario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Long idCompra;

    @Column(name = "codigo_compra", nullable = false, length = 80)
    private String codigoCompra;

    @Column(name = "codigo_generado", nullable = false)
    private Boolean codigoGenerado = Boolean.TRUE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra;

    @Enumerated(EnumType.STRING)
    @Column(name = "moneda", nullable = false, length = 10)
    private Moneda moneda = Moneda.PEN;

    @Column(name = "subtotal", nullable = false, precision = 18, scale = 4)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "descuento_total", nullable = false, precision = 18, scale = 4)
    private BigDecimal descuentoTotal = BigDecimal.ZERO;

    @Column(name = "impuesto_total", nullable = false, precision = 18, scale = 4)
    private BigDecimal impuestoTotal = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 18, scale = 4)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_compra", nullable = false, length = 30)
    private EstadoCompraInventario estadoCompra = EstadoCompraInventario.BORRADOR;

    @Column(name = "observacion", length = 500, columnDefinition = "nvarchar(500)")
    private String observacion;

    @Column(name = "creado_por_id_usuario_ms1", nullable = false)
    private Long creadoPorIdUsuarioMs1;

    @Column(name = "confirmado_por_id_usuario_ms1")
    private Long confirmadoPorIdUsuarioMs1;

    @Column(name = "confirmado_at")
    private LocalDateTime confirmadoAt;
}