package com.upsjb.ms3.domain.entity;

import com.upsjb.ms3.domain.enums.EstadoSku;
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
@Table(name = "producto_sku")
public class ProductoSku extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sku")
    private Long idSku;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "codigo_sku", nullable = false, length = 100)
    private String codigoSku;

    @Column(name = "codigo_generado", nullable = false)
    private Boolean codigoGenerado = Boolean.TRUE;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "color", length = 80, columnDefinition = "nvarchar(80)")
    private String color;

    @Column(name = "talla", length = 50, columnDefinition = "nvarchar(50)")
    private String talla;

    @Column(name = "material", length = 120, columnDefinition = "nvarchar(120)")
    private String material;

    @Column(name = "modelo", length = 120, columnDefinition = "nvarchar(120)")
    private String modelo;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    @Column(name = "stock_maximo")
    private Integer stockMaximo;

    @Column(name = "peso_gramos", precision = 18, scale = 3)
    private BigDecimal pesoGramos;

    @Column(name = "alto_cm", precision = 18, scale = 3)
    private BigDecimal altoCm;

    @Column(name = "ancho_cm", precision = 18, scale = 3)
    private BigDecimal anchoCm;

    @Column(name = "largo_cm", precision = 18, scale = 3)
    private BigDecimal largoCm;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_sku", nullable = false, length = 30)
    private EstadoSku estadoSku = EstadoSku.ACTIVO;
}