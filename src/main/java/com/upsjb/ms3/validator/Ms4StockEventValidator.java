// ruta: src/main/java/com/upsjb/ms3/validator/Ms4StockEventValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.enums.Ms4StockEventType;
import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import org.springframework.stereotype.Component;

@Component
public class Ms4StockEventValidator {

    public void validateStockCommand(
            String idempotencyKey,
            Ms4StockEventType eventType,
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Long idSku,
            Long idAlmacen,
            Integer cantidad
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (!StringNormalizer.hasText(idempotencyKey)) {
            errors.add("idempotencyKey", "La idempotency key es obligatoria.", "REQUIRED", idempotencyKey);
        }

        if (eventType == null) {
            errors.add("eventType", "El tipo de evento MS4 es obligatorio.", "REQUIRED", null);
        }

        if (referenciaTipo == null) {
            errors.add("referenciaTipo", "El tipo de referencia es obligatorio.", "REQUIRED", null);
        } else if (!referenciaTipo.isMs4()) {
            errors.add("referenciaTipo", "La referencia debe pertenecer a MS4.", "INVALID_VALUE", referenciaTipo);
        }

        if (!StringNormalizer.hasText(referenciaIdExterno)) {
            errors.add("referenciaIdExterno", "La referencia externa de MS4 es obligatoria.", "REQUIRED", referenciaIdExterno);
        }

        if (idSku == null) {
            errors.add("idSku", "El SKU MS3 es obligatorio.", "REQUIRED", null);
        }

        if (idAlmacen == null) {
            errors.add("idAlmacen", "El almacén MS3 es obligatorio.", "REQUIRED", null);
        }

        if (cantidad == null || cantidad <= 0) {
            errors.add("cantidad", "La cantidad debe ser mayor a cero.", "INVALID_VALUE", cantidad);
        }

        errors.throwIfAny("El evento de stock recibido desde MS4 no es válido.");
    }

    public void validateNotProcessed(boolean alreadyProcessed) {
        if (alreadyProcessed) {
            throw new ConflictException(
                    "MS4_EVENTO_YA_PROCESADO",
                    "El evento de MS4 ya fue procesado previamente."
            );
        }
    }

    public void validateConsistentReference(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno
    ) {
        if (referenciaTipo == null || !StringNormalizer.hasText(referenciaIdExterno)) {
            throw new ConflictException(
                    "MS4_REFERENCIA_INCOMPLETA",
                    "El evento de MS4 requiere tipo de referencia e identificador externo."
            );
        }

        if (!referenciaTipo.isMs4()) {
            throw new ConflictException(
                    "MS4_REFERENCIA_INVALIDA",
                    "El evento debe usar una referencia de MS4."
            );
        }
    }
}