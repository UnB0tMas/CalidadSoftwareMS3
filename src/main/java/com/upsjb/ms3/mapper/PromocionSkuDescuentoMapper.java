// ruta: src/main/java/com/upsjb/ms3/mapper/PromocionSkuDescuentoMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.Promocion;
import com.upsjb.ms3.domain.entity.PromocionSkuDescuentoVersion;
import com.upsjb.ms3.domain.entity.PromocionVersion;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoUpdateRequestDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoResponseDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PromocionSkuDescuentoMapper {

    public PromocionSkuDescuentoVersion toEntity(
            PromocionSkuDescuentoCreateRequestDto request,
            PromocionVersion promocionVersion,
            ProductoSku sku
    ) {
        if (request == null) {
            return null;
        }

        PromocionSkuDescuentoVersion entity = new PromocionSkuDescuentoVersion();
        entity.setPromocionVersion(promocionVersion);
        entity.setSku(sku);
        entity.setTipoDescuento(request.tipoDescuento());
        entity.setValorDescuento(request.valorDescuento());
        entity.setPrecioFinalEstimado(request.precioFinalEstimado());
        entity.setMargenEstimado(request.margenEstimado());
        entity.setLimiteUnidades(request.limiteUnidades());
        entity.setPrioridad(defaultInteger(request.prioridad(), 1));

        return entity;
    }

    public void updateEntity(
            PromocionSkuDescuentoVersion entity,
            PromocionSkuDescuentoUpdateRequestDto request
    ) {
        if (entity == null || request == null) {
            return;
        }

        entity.setTipoDescuento(request.tipoDescuento());
        entity.setValorDescuento(request.valorDescuento());
        entity.setPrecioFinalEstimado(request.precioFinalEstimado());
        entity.setMargenEstimado(request.margenEstimado());
        entity.setLimiteUnidades(request.limiteUnidades());
        entity.setPrioridad(defaultInteger(request.prioridad(), 1));
    }

    public PromocionSkuDescuentoResponseDto toResponse(
            PromocionSkuDescuentoVersion entity,
            MoneyResponseDto precioBase,
            Moneda moneda
    ) {
        return toResponse(entity, precioBase, moneda, true);
    }

    public PromocionSkuDescuentoResponseDto toPublicResponse(
            PromocionSkuDescuentoVersion entity,
            MoneyResponseDto precioBase,
            Moneda moneda
    ) {
        return toResponse(entity, precioBase, moneda, false);
    }

    public PromocionSkuDescuentoResponseDto toResponse(PromocionSkuDescuentoVersion entity) {
        return toResponse(entity, null, Moneda.PEN, true);
    }

    private PromocionSkuDescuentoResponseDto toResponse(
            PromocionSkuDescuentoVersion entity,
            MoneyResponseDto precioBase,
            Moneda moneda,
            boolean includeInternalMargin
    ) {
        if (entity == null) {
            return null;
        }

        PromocionVersion version = entity.getPromocionVersion();
        Promocion promocion = version == null ? null : version.getPromocion();
        ProductoSku sku = entity.getSku();
        Producto producto = sku == null ? null : sku.getProducto();
        Moneda monedaFinal = moneda == null ? Moneda.PEN : moneda;

        return PromocionSkuDescuentoResponseDto.builder()
                .idPromocionSkuDescuentoVersion(entity.getIdPromocionSkuDescuentoVersion())
                .idPromocionVersion(version == null ? null : version.getIdPromocionVersion())
                .idPromocion(promocion == null ? null : promocion.getIdPromocion())
                .codigoPromocion(promocion == null ? null : promocion.getCodigo())
                .nombrePromocion(promocion == null ? null : promocion.getNombre())
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .tipoDescuento(entity.getTipoDescuento())
                .valorDescuento(entity.getValorDescuento())
                .precioBase(precioBase)
                .precioFinalEstimado(toMoney(entity.getPrecioFinalEstimado(), monedaFinal))
                .margenEstimado(includeInternalMargin ? toMoney(entity.getMargenEstimado(), monedaFinal) : null)
                .limiteUnidades(entity.getLimiteUnidades())
                .prioridad(entity.getPrioridad())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
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

    private Integer defaultInteger(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }
}