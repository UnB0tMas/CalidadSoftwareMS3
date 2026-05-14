// ruta: src/main/java/com/upsjb/ms3/mapper/PrecioSkuMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.PrecioSkuHistorial;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.dto.precio.request.PrecioSkuCreateRequestDto;
import com.upsjb.ms3.dto.precio.response.PrecioSkuHistorialResponseDto;
import com.upsjb.ms3.dto.precio.response.PrecioSkuResponseDto;
import com.upsjb.ms3.dto.shared.IdCodigoNombreResponseDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class PrecioSkuMapper {

    public PrecioSkuHistorial toEntity(
            PrecioSkuCreateRequestDto request,
            ProductoSku sku,
            LocalDateTime fechaInicio,
            Long creadoPorIdUsuarioMs1
    ) {
        if (request == null) {
            return null;
        }

        PrecioSkuHistorial entity = new PrecioSkuHistorial();
        entity.setSku(sku);
        entity.setPrecioVenta(request.precioVenta());
        entity.setMoneda(request.moneda());
        entity.setFechaInicio(fechaInicio == null ? request.fechaInicio() : fechaInicio);
        entity.setFechaFin(null);
        entity.setVigente(Boolean.TRUE);
        entity.setMotivo(request.motivo());
        entity.setCreadoPorIdUsuarioMs1(creadoPorIdUsuarioMs1);

        return entity;
    }

    public PrecioSkuResponseDto toResponse(PrecioSkuHistorial entity) {
        if (entity == null) {
            return null;
        }

        ProductoSku sku = entity.getSku();
        Producto producto = sku == null ? null : sku.getProducto();

        return PrecioSkuResponseDto.builder()
                .idPrecioHistorial(entity.getIdPrecioHistorial())
                .sku(toSkuReference(sku))
                .idProducto(producto == null ? null : producto.getIdProducto())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .precioVenta(toMoney(entity.getPrecioVenta(), entity.getMoneda()))
                .moneda(entity.getMoneda())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .vigente(entity.getVigente())
                .motivo(entity.getMotivo())
                .creadoPorIdUsuarioMs1(entity.getCreadoPorIdUsuarioMs1())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PrecioSkuHistorialResponseDto toHistorialResponse(PrecioSkuHistorial entity) {
        if (entity == null) {
            return null;
        }

        ProductoSku sku = entity.getSku();
        Producto producto = sku == null ? null : sku.getProducto();

        return PrecioSkuHistorialResponseDto.builder()
                .idPrecioHistorial(entity.getIdPrecioHistorial())
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .precioVenta(toMoney(entity.getPrecioVenta(), entity.getMoneda()))
                .moneda(entity.getMoneda())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .vigente(entity.getVigente())
                .motivo(entity.getMotivo())
                .creadoPorIdUsuarioMs1(entity.getCreadoPorIdUsuarioMs1())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void closeVigencia(
            PrecioSkuHistorial entity,
            LocalDateTime fechaFin
    ) {
        if (entity == null) {
            return;
        }

        entity.setFechaFin(fechaFin);
        entity.setVigente(Boolean.FALSE);
    }

    private IdCodigoNombreResponseDto toSkuReference(ProductoSku sku) {
        if (sku == null) {
            return null;
        }

        Producto producto = sku.getProducto();

        return IdCodigoNombreResponseDto.builder()
                .id(sku.getIdSku())
                .codigo(sku.getCodigoSku())
                .nombre(producto == null ? sku.getCodigoSku() : producto.getNombre())
                .estado(sku.getEstado())
                .build();
    }

    private MoneyResponseDto toMoney(BigDecimal amount, Moneda moneda) {
        if (amount == null || moneda == null) {
            return null;
        }

        return MoneyResponseDto.builder()
                .amount(amount)
                .currency(moneda.getCode())
                .formatted(moneda.getSymbol() + " " + amount)
                .build();
    }
}