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
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "producto_atributo_valor")
public class ProductoAtributoValor extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_atributo_valor")
    private Long idProductoAtributoValor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_atributo", nullable = false)
    private Atributo atributo;

    @Column(name = "valor_texto", length = 500, columnDefinition = "nvarchar(500)")
    private String valorTexto;

    @Column(name = "valor_numero", precision = 18, scale = 6)
    private BigDecimal valorNumero;

    @Column(name = "valor_boolean")
    private Boolean valorBoolean;

    @Column(name = "valor_fecha")
    private LocalDate valorFecha;
}