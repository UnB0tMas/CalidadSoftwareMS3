// ruta: src/main/java/com/upsjb/ms3/validator/PromocionSkuDescuentoValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.config.AppPropertiesConfig;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.PromocionVersion;
import com.upsjb.ms3.domain.enums.TipoDescuento;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.MoneyUtil;
import com.upsjb.ms3.util.PercentageUtil;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromocionSkuDescuentoValidator {

    private final AppPropertiesConfig appProperties;

    public void validateCreate(
            PromocionVersion promocionVersion,
            ProductoSku sku,
            TipoDescuento tipoDescuento,
            BigDecimal valorDescuento,
            BigDecimal precioBase,
            BigDecimal costoEstimado,
            Integer limiteUnidades,
            Integer prioridad,
            boolean duplicatedSkuInVersion
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateVersionForDiscount(promocionVersion, errors);
        validateSku(sku, errors);
        validateBasicDiscountFields(tipoDescuento, valorDescuento, limiteUnidades, prioridad, errors);

        errors.throwIfAny("No se puede registrar el descuento de SKU en la promoción.");

        if (duplicatedSkuInVersion) {
            throw new ConflictException(
                    "PROMOCION_SKU_DUPLICADO",
                    "El SKU ya tiene un descuento registrado en esta versión de promoción."
            );
        }

        validateDiscountValue(tipoDescuento, valorDescuento, precioBase);
        validateMargin(tipoDescuento, valorDescuento, precioBase, costoEstimado);
    }

    public void validateUpdate(
            PromocionVersion promocionVersion,
            TipoDescuento tipoDescuento,
            BigDecimal valorDescuento,
            BigDecimal precioBase,
            BigDecimal costoEstimado
    ) {
        validateUpdate(
                promocionVersion,
                tipoDescuento,
                valorDescuento,
                precioBase,
                costoEstimado,
                null,
                null
        );
    }

    public void validateUpdate(
            PromocionVersion promocionVersion,
            TipoDescuento tipoDescuento,
            BigDecimal valorDescuento,
            BigDecimal precioBase,
            BigDecimal costoEstimado,
            Integer limiteUnidades,
            Integer prioridad
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateVersionForDiscount(promocionVersion, errors);
        validateBasicDiscountFields(tipoDescuento, valorDescuento, limiteUnidades, prioridad, errors);

        errors.throwIfAny("No se puede actualizar el descuento de SKU en la promoción.");

        if (!promocionVersion.getEstadoPromocion().isEditable()) {
            throw new ConflictException(
                    "PROMOCION_DESCUENTO_NO_EDITABLE",
                    "No se puede modificar el descuento porque la versión de promoción no es editable."
            );
        }

        validateDiscountValue(tipoDescuento, valorDescuento, precioBase);
        validateMargin(tipoDescuento, valorDescuento, precioBase, costoEstimado);
    }

    public void requireActive(PromocionVersion promocionVersion) {
        if (promocionVersion == null) {
            throw new NotFoundException(
                    "PROMOCION_VERSION_NO_ENCONTRADA",
                    "Versión de promoción no encontrada."
            );
        }

        if (!promocionVersion.isActivo()) {
            throw new NotFoundException(
                    "PROMOCION_VERSION_INACTIVA",
                    "La versión de promoción no está activa."
            );
        }
    }

    private void validateVersionForDiscount(PromocionVersion promocionVersion, ValidationErrorCollector errors) {
        if (promocionVersion == null) {
            errors.add("promocionVersion", "La versión de promoción es obligatoria.", "REQUIRED", null);
            return;
        }

        if (!promocionVersion.isActivo()) {
            errors.add(
                    "promocionVersion",
                    "La versión de promoción debe estar activa.",
                    "INACTIVE",
                    promocionVersion.getIdPromocionVersion()
            );
            return;
        }

        if (promocionVersion.getEstadoPromocion() == null) {
            errors.add(
                    "estadoPromocion",
                    "El estado de promoción de la versión es obligatorio.",
                    "REQUIRED",
                    null
            );
            return;
        }

        if (!promocionVersion.getEstadoPromocion().isEditable()) {
            errors.add(
                    "estadoPromocion",
                    "La versión de promoción no es editable.",
                    "INVALID_STATE",
                    promocionVersion.getEstadoPromocion()
            );
        }
    }

    private void validateSku(ProductoSku sku, ValidationErrorCollector errors) {
        if (sku == null) {
            errors.add("sku", "El SKU es obligatorio.", "REQUIRED", null);
            return;
        }

        if (!sku.isActivo()) {
            errors.add("sku", "El SKU debe estar activo.", "INACTIVE", sku.getIdSku());
        }
    }

    private void validateBasicDiscountFields(
            TipoDescuento tipoDescuento,
            BigDecimal valorDescuento,
            Integer limiteUnidades,
            Integer prioridad,
            ValidationErrorCollector errors
    ) {
        if (tipoDescuento == null) {
            errors.add("tipoDescuento", "El tipo de descuento es obligatorio.", "REQUIRED", null);
        }

        if (valorDescuento == null || valorDescuento.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(
                    "valorDescuento",
                    "El valor del descuento debe ser mayor a cero.",
                    "INVALID_VALUE",
                    valorDescuento
            );
        }

        if (limiteUnidades != null && limiteUnidades <= 0) {
            errors.add("limiteUnidades", "El límite de unidades debe ser mayor a cero.", "INVALID_VALUE", limiteUnidades);
        }

        if (prioridad != null && prioridad <= 0) {
            errors.add("prioridad", "La prioridad debe ser mayor a cero.", "INVALID_VALUE", prioridad);
        }
    }

    private void validateDiscountValue(
            TipoDescuento tipoDescuento,
            BigDecimal valorDescuento,
            BigDecimal precioBase
    ) {
        if (tipoDescuento == null || valorDescuento == null) {
            throw new ConflictException(
                    "DESCUENTO_INVALIDO",
                    "El tipo y valor de descuento son obligatorios."
            );
        }

        if (tipoDescuento.isPorcentaje() && !PercentageUtil.isValidPercentage(valorDescuento)) {
            throw new ConflictException(
                    "PORCENTAJE_DESCUENTO_INVALIDO",
                    "El porcentaje de descuento debe estar entre 0 y 100."
            );
        }

        if (tipoDescuento.isMontoFijo()) {
            if (!MoneyUtil.isValidPositiveAmount(valorDescuento)) {
                throw new ConflictException(
                        "MONTO_DESCUENTO_INVALIDO",
                        "El monto fijo de descuento debe ser mayor a cero."
                );
            }

            if (precioBase != null && valorDescuento.compareTo(precioBase) > 0) {
                throw new ConflictException(
                        "DESCUENTO_SUPERA_PRECIO",
                        "El descuento fijo no puede superar el precio base."
                );
            }
        }

        if (tipoDescuento.isPrecioFinal() && !MoneyUtil.isValidPositiveAmount(valorDescuento)) {
            throw new ConflictException(
                    "PRECIO_FINAL_INVALIDO",
                    "El precio final de promoción debe ser mayor a cero."
            );
        }
    }

    private void validateMargin(
            TipoDescuento tipoDescuento,
            BigDecimal valorDescuento,
            BigDecimal precioBase,
            BigDecimal costoEstimado
    ) {
        if (appProperties.getPromotion().isAllowNegativeMargin()) {
            return;
        }

        if (precioBase == null || costoEstimado == null || costoEstimado.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal precioFinal = resolveFinalPrice(tipoDescuento, valorDescuento, precioBase);

        if (precioFinal.compareTo(costoEstimado) < 0) {
            throw new ConflictException(
                    "PROMOCION_MARGEN_NEGATIVO",
                    "La promoción genera margen negativo y la configuración actual no lo permite."
            );
        }
    }

    private BigDecimal resolveFinalPrice(
            TipoDescuento tipoDescuento,
            BigDecimal valorDescuento,
            BigDecimal precioBase
    ) {
        if (tipoDescuento.isPorcentaje()) {
            return PercentageUtil.applyDiscount(precioBase, valorDescuento);
        }

        if (tipoDescuento.isMontoFijo()) {
            return MoneyUtil.applyDiscountAmount(precioBase, valorDescuento);
        }

        return MoneyUtil.normalize(valorDescuento);
    }
}