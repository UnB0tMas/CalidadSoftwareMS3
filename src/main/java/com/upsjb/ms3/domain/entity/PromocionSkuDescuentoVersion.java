package com.upsjb.ms3.domain.entity;

import com.upsjb.ms3.domain.enums.TipoDescuento;
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
@Table(name = "promocion_sku_descuento_version")
public class PromocionSkuDescuentoVersion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promocion_sku_descuento_version")
    private Long idPromocionSkuDescuentoVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_promocion_version", nullable = false)
    private PromocionVersion promocionVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sku", nullable = false)
    private ProductoSku sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento", nullable = false, length = 30)
    private TipoDescuento tipoDescuento;

    @Column(name = "valor_descuento", nullable = false, precision = 18, scale = 4)
    private BigDecimal valorDescuento;

    @Column(name = "precio_final_estimado", precision = 18, scale = 4)
    private BigDecimal precioFinalEstimado;

    @Column(name = "margen_estimado", precision = 18, scale = 4)
    private BigDecimal margenEstimado;

    @Column(name = "limite_unidades")
    private Integer limiteUnidades;

    @Column(name = "prioridad", nullable = false)
    private Integer prioridad = 1;
}