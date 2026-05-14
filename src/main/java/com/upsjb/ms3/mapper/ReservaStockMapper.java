// ruta: src/main/java/com/upsjb/ms3/mapper/ReservaStockMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.ReservaStock;
import com.upsjb.ms3.domain.enums.EstadoReservaStock;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockCreateRequestDto;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockMs4RequestDto;
import com.upsjb.ms3.dto.inventario.reserva.response.ReservaStockResponseDto;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class ReservaStockMapper {

    public ReservaStock toEntity(
            ReservaStockCreateRequestDto request,
            ProductoSku sku,
            Almacen almacen,
            String codigoReserva,
            Long reservadoPorIdUsuarioMs1,
            LocalDateTime reservadoAt,
            LocalDateTime expiresAt
    ) {
        if (request == null) {
            return null;
        }

        ReservaStock entity = new ReservaStock();
        entity.setCodigoReserva(codigoReserva);
        entity.setCodigoGenerado(Boolean.TRUE);
        entity.setSku(sku);
        entity.setAlmacen(almacen);
        entity.setReferenciaTipo(request.referenciaTipo());
        entity.setReferenciaIdExterno(request.referenciaIdExterno());
        entity.setCantidad(request.cantidad());
        entity.setEstadoReserva(EstadoReservaStock.RESERVADA);
        entity.setReservadoPorIdUsuarioMs1(reservadoPorIdUsuarioMs1);
        entity.setReservadoAt(reservadoAt == null ? LocalDateTime.now() : reservadoAt);
        entity.setExpiresAt(expiresAt == null ? request.expiresAt() : expiresAt);
        entity.setMotivo(request.motivo());

        return entity;
    }

    public ReservaStock toEntity(
            ReservaStockMs4RequestDto request,
            ProductoSku sku,
            Almacen almacen,
            String codigoReserva,
            LocalDateTime reservadoAt,
            LocalDateTime expiresAt
    ) {
        if (request == null) {
            return null;
        }

        ReservaStock entity = new ReservaStock();
        entity.setCodigoReserva(codigoReserva);
        entity.setCodigoGenerado(Boolean.TRUE);
        entity.setSku(sku);
        entity.setAlmacen(almacen);
        entity.setReferenciaTipo(request.referenciaTipo());
        entity.setReferenciaIdExterno(request.referenciaIdExterno());
        entity.setCantidad(request.cantidad());
        entity.setEstadoReserva(EstadoReservaStock.RESERVADA);
        entity.setReservadoPorIdUsuarioMs1(request.actorIdUsuarioMs1());
        entity.setReservadoAt(reservadoAt == null
                ? defaultDateTime(request.occurredAt())
                : reservadoAt);
        entity.setExpiresAt(expiresAt == null ? request.expiresAt() : expiresAt);
        entity.setMotivo(request.motivo());

        return entity;
    }

    public void markConfirmada(
            ReservaStock entity,
            Long confirmadoPorIdUsuarioMs1,
            LocalDateTime confirmadoAt,
            String motivo
    ) {
        if (entity == null) {
            return;
        }

        entity.setEstadoReserva(EstadoReservaStock.CONFIRMADA);
        entity.setConfirmadoPorIdUsuarioMs1(confirmadoPorIdUsuarioMs1);
        entity.setConfirmadoAt(confirmadoAt == null ? LocalDateTime.now() : confirmadoAt);
        entity.setMotivo(motivo);
    }

    public void markLiberada(
            ReservaStock entity,
            Long liberadoPorIdUsuarioMs1,
            LocalDateTime liberadoAt,
            String motivo
    ) {
        if (entity == null) {
            return;
        }

        entity.setEstadoReserva(EstadoReservaStock.LIBERADA);
        entity.setLiberadoPorIdUsuarioMs1(liberadoPorIdUsuarioMs1);
        entity.setLiberadoAt(liberadoAt == null ? LocalDateTime.now() : liberadoAt);
        entity.setMotivo(motivo);
    }

    public void markVencida(ReservaStock entity, LocalDateTime liberadoAt) {
        if (entity == null) {
            return;
        }

        entity.setEstadoReserva(EstadoReservaStock.VENCIDA);
        entity.setLiberadoAt(liberadoAt == null ? LocalDateTime.now() : liberadoAt);
    }

    public ReservaStockResponseDto toResponse(ReservaStock entity) {
        if (entity == null) {
            return null;
        }

        ProductoSku sku = entity.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        Almacen almacen = entity.getAlmacen();

        return ReservaStockResponseDto.builder()
                .idReservaStock(entity.getIdReservaStock())
                .codigoReserva(entity.getCodigoReserva())
                .codigoGenerado(entity.getCodigoGenerado())
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .idAlmacen(almacen == null ? null : almacen.getIdAlmacen())
                .codigoAlmacen(almacen == null ? null : almacen.getCodigo())
                .nombreAlmacen(almacen == null ? null : almacen.getNombre())
                .referenciaTipo(entity.getReferenciaTipo())
                .referenciaIdExterno(entity.getReferenciaIdExterno())
                .cantidad(entity.getCantidad())
                .estadoReserva(entity.getEstadoReserva())
                .reservadoPorIdUsuarioMs1(entity.getReservadoPorIdUsuarioMs1())
                .confirmadoPorIdUsuarioMs1(entity.getConfirmadoPorIdUsuarioMs1())
                .liberadoPorIdUsuarioMs1(entity.getLiberadoPorIdUsuarioMs1())
                .reservadoAt(entity.getReservadoAt())
                .confirmadoAt(entity.getConfirmadoAt())
                .liberadoAt(entity.getLiberadoAt())
                .expiresAt(entity.getExpiresAt())
                .expirada(isExpirada(entity))
                .motivo(entity.getMotivo())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Boolean isExpirada(ReservaStock entity) {
        if (entity == null || entity.getExpiresAt() == null) {
            return false;
        }

        return EstadoReservaStock.RESERVADA.equals(entity.getEstadoReserva())
                && entity.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private LocalDateTime defaultDateTime(LocalDateTime value) {
        return value == null ? LocalDateTime.now() : value;
    }
}