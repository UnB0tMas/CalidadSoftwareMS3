// ruta: src/main/java/com/upsjb/ms3/validator/KardexValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.shared.exception.ConflictException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class KardexValidator {

    public void validateFilter(
            Long idSku,
            Long idAlmacen,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    ) {
        if (idSku == null && idAlmacen == null && fechaInicio == null && fechaFin == null) {
            throw new ConflictException(
                    "KARDEX_FILTRO_OBLIGATORIO",
                    "Debe indicar al menos SKU, almacén o rango de fechas para consultar kardex."
            );
        }

        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new ConflictException(
                    "KARDEX_RANGO_FECHAS_INVALIDO",
                    "La fecha fin no puede ser menor que la fecha inicio."
            );
        }
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
        boolean hasTipo = referenciaTipo != null && !referenciaTipo.isBlank();
        boolean hasId = referenciaIdExterno != null && !referenciaIdExterno.isBlank();

        if (hasTipo != hasId) {
            throw new ConflictException(
                    "KARDEX_REFERENCIA_INCOMPLETA",
                    "Para buscar por referencia debe indicar tipo e identificador externo."
            );
        }
    }
}