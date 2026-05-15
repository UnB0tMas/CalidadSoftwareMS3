// ruta: src/main/java/com/upsjb/ms3/validator/KardexValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.dto.inventario.movimiento.filter.KardexFilterDto;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.ValidationException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class KardexValidator {

    public void validateFilter(KardexFilterDto filter) {
        if (filter == null) {
            return;
        }

        validateReferenceSearch(filter.referenciaTipo(), filter.referenciaIdExterno());

        DateRangeFilterDto dateRange = filter.fechaMovimiento();
        validateDateRange(
                dateRange == null ? null : dateRange.fechaInicio(),
                dateRange == null ? null : dateRange.fechaFin()
        );

        validateCodigoMovimientoIfPresent(filter.codigoMovimiento());
    }

    public void validateFilter(
            Long idSku,
            Long idAlmacen,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    ) {
        validateDateRange(fechaInicio, fechaFin);
    }

    public void validateCanViewCost(boolean canViewCost) {
        if (!canViewCost) {
            throw new ConflictException(
                    "KARDEX_COSTO_NO_AUTORIZADO",
                    "No tiene autorización para visualizar costos del kardex."
            );
        }
    }

    public void validateReferenceSearch(String referenciaTipo, String referenciaIdExterno) {
        boolean hasTipo = StringUtils.hasText(referenciaTipo);
        boolean hasId = StringUtils.hasText(referenciaIdExterno);

        if (hasTipo != hasId) {
            throw new ConflictException(
                    "KARDEX_REFERENCIA_INCOMPLETA",
                    "Para buscar por referencia debe indicar tipo e identificador externo."
            );
        }
    }

    public void validateCodigoMovimiento(String codigoMovimiento) {
        if (!StringUtils.hasText(codigoMovimiento)) {
            throw new ValidationException(
                    "KARDEX_CODIGO_MOVIMIENTO_REQUERIDO",
                    "Debe indicar el código de movimiento solicitado."
            );
        }

        validateCodigoMovimientoIfPresent(codigoMovimiento);
    }

    private void validateCodigoMovimientoIfPresent(String codigoMovimiento) {
        if (!StringUtils.hasText(codigoMovimiento)) {
            return;
        }

        if (codigoMovimiento.trim().length() > 100) {
            throw new ValidationException(
                    "KARDEX_CODIGO_MOVIMIENTO_INVALIDO",
                    "El código de movimiento no debe superar 100 caracteres."
            );
        }
    }

    private void validateDateRange(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new ConflictException(
                    "KARDEX_RANGO_FECHAS_INVALIDO",
                    "La fecha fin no puede ser menor que la fecha inicio."
            );
        }
    }
}