// ruta: src/main/java/com/upsjb/ms3/validator/Ms4StockEventValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.ReservaStock;
import com.upsjb.ms3.domain.enums.Ms4StockEventType;
import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import java.util.Objects;
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
        validateStockCommand(
                idempotencyKey,
                eventType,
                null,
                referenciaTipo,
                referenciaIdExterno,
                idSku,
                idAlmacen,
                cantidad,
                true
        );
    }

    public void validateStockCommand(
            String idempotencyKey,
            Ms4StockEventType eventType,
            Ms4StockEventType expectedEventType,
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Long idSku,
            Long idAlmacen,
            Integer cantidad,
            boolean cantidadObligatoria
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (!StringNormalizer.hasText(idempotencyKey)) {
            errors.add("idempotencyKey", "La idempotency key es obligatoria.", "REQUIRED", idempotencyKey);
        }

        if (eventType == null) {
            errors.add("eventType", "El tipo de evento MS4 es obligatorio.", "REQUIRED", null);
        } else if (expectedEventType != null && eventType != expectedEventType) {
            errors.add(
                    "eventType",
                    "El tipo de evento MS4 no corresponde a la operación solicitada.",
                    "INVALID_VALUE",
                    eventType
            );
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
            errors.add("sku", "El SKU MS3 es obligatorio.", "REQUIRED", null);
        }

        if (idAlmacen == null) {
            errors.add("almacen", "El almacén MS3 es obligatorio.", "REQUIRED", null);
        }

        if (cantidadObligatoria && cantidad == null) {
            errors.add("cantidad", "La cantidad es obligatoria.", "REQUIRED", null);
        } else if (cantidad != null && cantidad <= 0) {
            errors.add("cantidad", "La cantidad debe ser mayor a cero.", "INVALID_VALUE", cantidad);
        }

        errors.throwIfAny("El evento de stock recibido desde MS4 no es válido.");
    }

    public void validateReservaMatchesCommand(
            ReservaStock reserva,
            Long idSku,
            Long idAlmacen,
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Integer cantidadInformada
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (reserva == null) {
            errors.add("reserva", "La reserva de stock es obligatoria.", "REQUIRED", null);
            errors.throwIfAny("La reserva asociada al evento MS4 no es coherente.");
            return;
        }

        Long reservaSkuId = reserva.getSku() == null ? null : reserva.getSku().getIdSku();
        Long reservaAlmacenId = reserva.getAlmacen() == null ? null : reserva.getAlmacen().getIdAlmacen();

        if (!Objects.equals(reservaSkuId, idSku)) {
            errors.add("sku", "La reserva no corresponde al SKU informado por MS4.", "INVALID_VALUE", idSku);
        }

        if (!Objects.equals(reservaAlmacenId, idAlmacen)) {
            errors.add("almacen", "La reserva no corresponde al almacén informado por MS4.", "INVALID_VALUE", idAlmacen);
        }

        if (!Objects.equals(reserva.getReferenciaTipo(), referenciaTipo)) {
            errors.add(
                    "referenciaTipo",
                    "La reserva no corresponde al tipo de referencia informado por MS4.",
                    "INVALID_VALUE",
                    referenciaTipo
            );
        }

        if (!Objects.equals(
                normalize(reserva.getReferenciaIdExterno()),
                normalize(referenciaIdExterno)
        )) {
            errors.add(
                    "referenciaIdExterno",
                    "La reserva no corresponde a la referencia externa informada por MS4.",
                    "INVALID_VALUE",
                    referenciaIdExterno
            );
        }

        if (cantidadInformada != null && !Objects.equals(reserva.getCantidad(), cantidadInformada)) {
            errors.add(
                    "cantidad",
                    "La cantidad informada por MS4 no coincide con la cantidad reservada.",
                    "INVALID_VALUE",
                    cantidadInformada
            );
        }

        errors.throwIfAny("La reserva asociada al evento MS4 no es coherente.");
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

    private String normalize(String value) {
        return StringNormalizer.hasText(value) ? value.trim() : null;
    }
}