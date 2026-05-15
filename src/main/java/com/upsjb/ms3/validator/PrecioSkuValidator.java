// ruta: src/main/java/com/upsjb/ms3/validator/PrecioSkuValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.PrecioSkuHistorial;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.MoneyUtil;
import com.upsjb.ms3.util.StringNormalizer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class PrecioSkuValidator {

    public void validateCreate(
            ProductoSku sku,
            BigDecimal precioVenta,
            Moneda moneda,
            LocalDateTime fechaInicio,
            String motivo,
            Long creadoPorIdUsuarioMs1
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (sku == null) {
            errors.add("sku", "El SKU es obligatorio.", "REQUIRED", null);
        } else if (!sku.isActivo()) {
            errors.add("sku", "El SKU debe estar activo.", "INACTIVE", sku.getIdSku());
        }

        if (!MoneyUtil.isValidPositiveAmount(precioVenta)) {
            errors.add("precioVenta", "El precio de venta debe ser mayor a cero.", "INVALID_VALUE", precioVenta);
        }

        if (moneda == null) {
            errors.add("moneda", "La moneda es obligatoria.", "REQUIRED", null);
        }

        if (fechaInicio == null) {
            errors.add("fechaInicio", "La fecha de inicio del precio es obligatoria.", "REQUIRED", null);
        }

        if (!StringNormalizer.hasText(motivo)) {
            errors.add("motivo", "El motivo del cambio de precio es obligatorio.", "REQUIRED", motivo);
        }

        if (creadoPorIdUsuarioMs1 == null) {
            errors.add("creadoPorIdUsuarioMs1", "El usuario creador es obligatorio.", "REQUIRED", null);
        }

        errors.throwIfAny("No se puede registrar el precio del SKU.");
    }

    public void validateNoCurrentPriceConflict(boolean hasCurrentPriceWithSameStartDate) {
        if (hasCurrentPriceWithSameStartDate) {
            throw new ConflictException(
                    "PRECIO_VIGENTE_CON_MISMA_FECHA",
                    "Ya existe un precio vigente para el SKU con la misma fecha de inicio."
            );
        }
    }

    public void validateCanCloseCurrent(PrecioSkuHistorial currentPrice, LocalDateTime newStartDate) {
        if (currentPrice == null) {
            return;
        }

        requireActive(currentPrice);

        if (!Boolean.TRUE.equals(currentPrice.getVigente())) {
            throw new ConflictException(
                    "PRECIO_ACTUAL_NO_VIGENTE",
                    "El precio actual no se encuentra vigente."
            );
        }

        if (newStartDate != null
                && currentPrice.getFechaInicio() != null
                && (newStartDate.isBefore(currentPrice.getFechaInicio())
                || newStartDate.isEqual(currentPrice.getFechaInicio()))) {
            throw new ConflictException(
                    "PRECIO_FECHA_INICIO_INVALIDA",
                    "La fecha de inicio del nuevo precio debe ser mayor que la del precio vigente."
            );
        }
    }

    public void validateNewVersion(
            ProductoSku sku,
            BigDecimal precioVenta,
            Moneda moneda,
            LocalDateTime fechaInicio,
            String motivo,
            Long creadoPorIdUsuarioMs1,
            PrecioSkuHistorial currentPrice,
            boolean sameStartDateExists
    ) {
        validateCreate(
                sku,
                precioVenta,
                moneda,
                fechaInicio,
                motivo,
                creadoPorIdUsuarioMs1
        );

        validateNoCurrentPriceConflict(sameStartDateExists);
        validateCanCloseCurrent(currentPrice, fechaInicio);
    }

    public void requireActive(PrecioSkuHistorial precio) {
        if (precio == null) {
            throw new NotFoundException(
                    "PRECIO_SKU_NO_ENCONTRADO",
                    "No se encontró el registro solicitado."
            );
        }

        if (!precio.isActivo()) {
            throw new NotFoundException(
                    "PRECIO_SKU_INACTIVO",
                    "No se puede completar la operación porque el registro está inactivo."
            );
        }
    }
}