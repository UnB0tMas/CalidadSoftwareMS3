package com.upsjb.ms3.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "almacen")
public class Almacen extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_almacen")
    private Long idAlmacen;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150, columnDefinition = "nvarchar(150)")
    private String nombre;

    @Column(name = "direccion", length = 300, columnDefinition = "nvarchar(300)")
    private String direccion;

    @Column(name = "principal", nullable = false)
    private Boolean principal = Boolean.FALSE;

    @Column(name = "permite_venta", nullable = false)
    private Boolean permiteVenta = Boolean.TRUE;

    @Column(name = "permite_compra", nullable = false)
    private Boolean permiteCompra = Boolean.TRUE;

    @Column(name = "observacion", length = 500, columnDefinition = "nvarchar(500)")
    private String observacion;
}