// ruta: src/main/java/com/upsjb/ms3/mapper/AlmacenMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.dto.inventario.almacen.request.AlmacenCreateRequestDto;
import com.upsjb.ms3.dto.inventario.almacen.request.AlmacenUpdateRequestDto;
import com.upsjb.ms3.dto.inventario.almacen.response.AlmacenDetailResponseDto;
import com.upsjb.ms3.dto.inventario.almacen.response.AlmacenResponseDto;
import com.upsjb.ms3.dto.reference.response.AlmacenOptionDto;
import org.springframework.stereotype.Component;

@Component
public class AlmacenMapper {

    public Almacen toEntity(AlmacenCreateRequestDto request) {
        if (request == null) {
            return null;
        }

        Almacen entity = new Almacen();
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setDireccion(request.direccion());
        entity.setPrincipal(defaultBoolean(request.principal(), false));
        entity.setPermiteVenta(defaultBoolean(request.permiteVenta(), true));
        entity.setPermiteCompra(defaultBoolean(request.permiteCompra(), true));
        entity.setObservacion(request.observacion());

        return entity;
    }

    public void updateEntity(Almacen entity, AlmacenUpdateRequestDto request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setDireccion(request.direccion());
        entity.setPrincipal(defaultBoolean(request.principal(), Boolean.FALSE));
        entity.setPermiteVenta(defaultBoolean(request.permiteVenta(), Boolean.TRUE));
        entity.setPermiteCompra(defaultBoolean(request.permiteCompra(), Boolean.TRUE));
        entity.setObservacion(request.observacion());
    }

    public AlmacenResponseDto toResponse(Almacen entity) {
        if (entity == null) {
            return null;
        }

        return AlmacenResponseDto.builder()
                .idAlmacen(entity.getIdAlmacen())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .direccion(entity.getDireccion())
                .principal(entity.getPrincipal())
                .permiteVenta(entity.getPermiteVenta())
                .permiteCompra(entity.getPermiteCompra())
                .observacion(entity.getObservacion())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public AlmacenDetailResponseDto toDetailResponse(
            Almacen entity,
            Long cantidadSkusConStock,
            Integer stockFisicoTotal,
            Integer stockReservadoTotal,
            Integer stockDisponibleTotal
    ) {
        if (entity == null) {
            return null;
        }

        return AlmacenDetailResponseDto.builder()
                .idAlmacen(entity.getIdAlmacen())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .direccion(entity.getDireccion())
                .principal(entity.getPrincipal())
                .permiteVenta(entity.getPermiteVenta())
                .permiteCompra(entity.getPermiteCompra())
                .observacion(entity.getObservacion())
                .estado(entity.getEstado())
                .cantidadSkusConStock(defaultLong(cantidadSkusConStock))
                .stockFisicoTotal(defaultInteger(stockFisicoTotal))
                .stockReservadoTotal(defaultInteger(stockReservadoTotal))
                .stockDisponibleTotal(defaultInteger(stockDisponibleTotal))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public AlmacenOptionDto toOption(Almacen entity) {
        if (entity == null) {
            return null;
        }

        return AlmacenOptionDto.builder()
                .idAlmacen(entity.getIdAlmacen())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .direccion(entity.getDireccion())
                .principal(entity.getPrincipal())
                .permiteVenta(entity.getPermiteVenta())
                .permiteCompra(entity.getPermiteCompra())
                .estado(entity.getEstado())
                .build();
    }

    private Boolean defaultBoolean(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}