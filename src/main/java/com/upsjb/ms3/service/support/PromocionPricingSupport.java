// ruta: src/main/java/com/upsjb/ms3/service/support/PromocionPricingSupport.java
package com.upsjb.ms3.service.support;

import com.upsjb.ms3.domain.entity.PrecioSkuHistorial;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.domain.enums.TipoDescuento;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import com.upsjb.ms3.repository.PrecioSkuHistorialRepository;
import com.upsjb.ms3.repository.StockSkuRepository;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.util.MoneyUtil;
import com.upsjb.ms3.util.PercentageUtil;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromocionPricingSupport {

    private final PrecioSkuHistorialRepository precioSkuHistorialRepository;
    private final StockSkuRepository stockSkuRepository;

    public PrecioSkuHistorial currentPriceRequired(ProductoSku sku) {
        if (sku == null || sku.getIdSku() == null) {
            throw new ValidationException(
                    "SKU_REFERENCIA_REQUERIDA",
                    "Debe indicar el SKU."
            );
        }

        return precioSkuHistorialRepository
                .findFirstBySku_IdSkuAndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPrecioHistorialDesc(
                        sku.getIdSku()
                )
                .orElseThrow(() -> new ConflictException(
                        "SKU_SIN_PRECIO_VIGENTE",
                        "No se puede registrar el descuento porque el SKU no tiene precio vigente."
                ));
    }

    public BigDecimal costoPromedioEstimado(ProductoSku sku) {
        if (sku == null || sku.getIdSku() == null) {
            return null;
        }

        return stockSkuRepository
                .findBySku_IdSkuAndEstadoTrueOrderByAlmacen_PrincipalDescAlmacen_NombreAsc(sku.getIdSku())
                .stream()
                .map(StockSku::getCostoPromedioActual)
                .filter(value -> value != null && value.compareTo(BigDecimal.ZERO) > 0)
                .findFirst()
                .orElse(null);
    }

    public BigDecimal resolvePrecioFinal(
            TipoDescuento tipoDescuento,
            BigDecimal valorDescuento,
            BigDecimal precioBase
    ) {
        if (tipoDescuento == null || valorDescuento == null || precioBase == null) {
            return null;
        }

        if (tipoDescuento.isPorcentaje()) {
            return PercentageUtil.applyDiscount(precioBase, valorDescuento);
        }

        if (tipoDescuento.isMontoFijo()) {
            return MoneyUtil.applyDiscountAmount(precioBase, valorDescuento);
        }

        return MoneyUtil.normalize(valorDescuento);
    }

    public BigDecimal resolveMargen(BigDecimal precioFinal, BigDecimal costoEstimado) {
        if (precioFinal == null || costoEstimado == null) {
            return null;
        }

        return MoneyUtil.normalize(precioFinal.subtract(costoEstimado));
    }

    public MoneyResponseDto toMoney(BigDecimal amount, Moneda moneda) {
        if (amount == null || moneda == null) {
            return null;
        }

        BigDecimal normalized = MoneyUtil.normalize(amount);

        return MoneyResponseDto.builder()
                .amount(normalized)
                .currency(moneda.getCode())
                .formatted(moneda.getSymbol() + " " + normalized)
                .build();
    }
}