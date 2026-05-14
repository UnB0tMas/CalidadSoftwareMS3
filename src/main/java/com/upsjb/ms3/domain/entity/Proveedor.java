package com.upsjb.ms3.domain.entity;

import com.upsjb.ms3.domain.enums.TipoDocumentoProveedor;
import com.upsjb.ms3.domain.enums.TipoProveedor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "proveedor")
public class Proveedor extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long idProveedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_proveedor", nullable = false, length = 30)
    private TipoProveedor tipoProveedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 30)
    private TipoDocumentoProveedor tipoDocumento;

    @Column(name = "numero_documento", length = 30)
    private String numeroDocumento;

    @Column(name = "ruc", length = 20)
    private String ruc;

    @Column(name = "razon_social", length = 200, columnDefinition = "nvarchar(200)")
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 200, columnDefinition = "nvarchar(200)")
    private String nombreComercial;

    @Column(name = "nombres", length = 150, columnDefinition = "nvarchar(150)")
    private String nombres;

    @Column(name = "apellidos", length = 150, columnDefinition = "nvarchar(150)")
    private String apellidos;

    @Column(name = "correo", length = 180, columnDefinition = "nvarchar(180)")
    private String correo;

    @Column(name = "telefono", length = 30, columnDefinition = "nvarchar(30)")
    private String telefono;

    @Column(name = "direccion", length = 300, columnDefinition = "nvarchar(300)")
    private String direccion;

    @Column(name = "observacion", length = 500, columnDefinition = "nvarchar(500)")
    private String observacion;

    @Column(name = "creado_por_id_usuario_ms1", nullable = false)
    private Long creadoPorIdUsuarioMs1;

    @Column(name = "actualizado_por_id_usuario_ms1")
    private Long actualizadoPorIdUsuarioMs1;
}