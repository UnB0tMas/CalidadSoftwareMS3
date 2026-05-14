// ruta: src/main/java/com/upsjb/ms3/mapper/ProveedorMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Proveedor;
import com.upsjb.ms3.domain.enums.TipoProveedor;
import com.upsjb.ms3.dto.proveedor.request.ProveedorCreateRequestDto;
import com.upsjb.ms3.dto.proveedor.request.ProveedorUpdateRequestDto;
import com.upsjb.ms3.dto.proveedor.response.ProveedorDetailResponseDto;
import com.upsjb.ms3.dto.proveedor.response.ProveedorResponseDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ProveedorMapper {

    public Proveedor toEntity(
            ProveedorCreateRequestDto request,
            Long creadoPorIdUsuarioMs1
    ) {
        if (request == null) {
            return null;
        }

        Proveedor entity = new Proveedor();
        entity.setTipoProveedor(request.tipoProveedor());
        entity.setTipoDocumento(request.tipoDocumento());
        entity.setNumeroDocumento(request.numeroDocumento());
        entity.setRuc(request.ruc());
        entity.setRazonSocial(request.razonSocial());
        entity.setNombreComercial(request.nombreComercial());
        entity.setNombres(request.nombres());
        entity.setApellidos(request.apellidos());
        entity.setCorreo(request.correo());
        entity.setTelefono(request.telefono());
        entity.setDireccion(request.direccion());
        entity.setObservacion(request.observacion());
        entity.setCreadoPorIdUsuarioMs1(creadoPorIdUsuarioMs1);

        return entity;
    }

    public void updateEntity(
            Proveedor entity,
            ProveedorUpdateRequestDto request,
            Long actualizadoPorIdUsuarioMs1
    ) {
        if (entity == null || request == null) {
            return;
        }

        entity.setTipoProveedor(request.tipoProveedor());
        entity.setTipoDocumento(request.tipoDocumento());
        entity.setNumeroDocumento(request.numeroDocumento());
        entity.setRuc(request.ruc());
        entity.setRazonSocial(request.razonSocial());
        entity.setNombreComercial(request.nombreComercial());
        entity.setNombres(request.nombres());
        entity.setApellidos(request.apellidos());
        entity.setCorreo(request.correo());
        entity.setTelefono(request.telefono());
        entity.setDireccion(request.direccion());
        entity.setObservacion(request.observacion());
        entity.setActualizadoPorIdUsuarioMs1(actualizadoPorIdUsuarioMs1);
    }

    public ProveedorResponseDto toResponse(Proveedor entity) {
        if (entity == null) {
            return null;
        }

        return ProveedorResponseDto.builder()
                .idProveedor(entity.getIdProveedor())
                .tipoProveedor(entity.getTipoProveedor())
                .tipoDocumento(entity.getTipoDocumento())
                .numeroDocumento(entity.getNumeroDocumento())
                .ruc(entity.getRuc())
                .razonSocial(entity.getRazonSocial())
                .nombreComercial(entity.getNombreComercial())
                .nombres(entity.getNombres())
                .apellidos(entity.getApellidos())
                .displayName(displayName(entity))
                .correo(entity.getCorreo())
                .telefono(entity.getTelefono())
                .direccion(entity.getDireccion())
                .observacion(entity.getObservacion())
                .creadoPorIdUsuarioMs1(entity.getCreadoPorIdUsuarioMs1())
                .actualizadoPorIdUsuarioMs1(entity.getActualizadoPorIdUsuarioMs1())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ProveedorDetailResponseDto toDetailResponse(
            Proveedor entity,
            Long cantidadCompras
    ) {
        if (entity == null) {
            return null;
        }

        return ProveedorDetailResponseDto.builder()
                .idProveedor(entity.getIdProveedor())
                .tipoProveedor(entity.getTipoProveedor())
                .tipoDocumento(entity.getTipoDocumento())
                .numeroDocumento(entity.getNumeroDocumento())
                .ruc(entity.getRuc())
                .razonSocial(entity.getRazonSocial())
                .nombreComercial(entity.getNombreComercial())
                .nombres(entity.getNombres())
                .apellidos(entity.getApellidos())
                .displayName(displayName(entity))
                .correo(entity.getCorreo())
                .telefono(entity.getTelefono())
                .direccion(entity.getDireccion())
                .observacion(entity.getObservacion())
                .creadoPorIdUsuarioMs1(entity.getCreadoPorIdUsuarioMs1())
                .actualizadoPorIdUsuarioMs1(entity.getActualizadoPorIdUsuarioMs1())
                .estado(entity.getEstado())
                .cantidadCompras(defaultLong(cantidadCompras))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public String displayName(Proveedor entity) {
        if (entity == null) {
            return null;
        }

        if (TipoProveedor.EMPRESA.equals(entity.getTipoProveedor())) {
            if (StringUtils.hasText(entity.getNombreComercial())) {
                return entity.getNombreComercial();
            }
            return entity.getRazonSocial();
        }

        String nombres = entity.getNombres() == null ? "" : entity.getNombres().trim();
        String apellidos = entity.getApellidos() == null ? "" : entity.getApellidos().trim();
        String fullName = (nombres + " " + apellidos).trim();

        return StringUtils.hasText(fullName) ? fullName : entity.getNumeroDocumento();
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}