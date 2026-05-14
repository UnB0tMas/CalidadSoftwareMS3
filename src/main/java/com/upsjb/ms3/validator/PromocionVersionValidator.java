// ruta: src/main/java/com/upsjb/ms3/validator/PromocionVersionValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Promocion;
import com.upsjb.ms3.domain.entity.PromocionVersion;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class PromocionVersionValidator {

    public void validateCreate(
            Promocion promocion,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            EstadoPromocion estadoPromocion,
            String motivo,
            Long creadoPorIdUsuarioMs1,
            boolean hasCurrentVersion,
            boolean hasOverlappingVersion
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (promocion == null) {
            errors.add("promocion", "La promoción es obligatoria.", "REQUIRED", null);
        } else if (!promocion.isActivo()) {
            errors.add("promocion", "La promoción debe estar activa.", "INACTIVE", promocion.getIdPromocion());
        }

        if (fechaInicio == null) {
            errors.add("fechaInicio", "La fecha de inicio es obligatoria.", "REQUIRED", null);
        }

        if (fechaFin == null) {
            errors.add("fechaFin", "La fecha fin es obligatoria.", "REQUIRED", null);
        }

        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            errors.add("fechaFin", "La fecha fin no puede ser menor que la fecha de inicio.", "INVALID_RANGE", fechaFin);
        }

        if (estadoPromocion == null) {
            errors.add("estadoPromocion", "El estado de promoción es obligatorio.", "REQUIRED", null);
        }

        if (!StringNormalizer.hasText(motivo)) {
            errors.add("motivo", "El motivo es obligatorio.", "REQUIRED", motivo);
        }

        if (creadoPorIdUsuarioMs1 == null) {
            errors.add("creadoPorIdUsuarioMs1", "El usuario creador es obligatorio.", "REQUIRED", null);
        }

        errors.throwIfAny("No se puede crear la versión de promoción.");

        if (hasCurrentVersion) {
            throw new ConflictException(
                    "PROMOCION_VERSION_VIGENTE_EXISTENTE",
                    "Ya existe una versión vigente para la promoción."
            );
        }

        if (hasOverlappingVersion) {
            throw new ConflictException(
                    "PROMOCION_VERSION_SUPERPUESTA",
                    "Ya existe una versión de promoción superpuesta en el rango de fechas."
            );
        }
    }

    public void validateCanActivate(PromocionVersion version, boolean hasDiscounts) {
        requireActive(version);

        if (!version.getEstadoPromocion().isEditable()) {
            throw new ConflictException(
                    "PROMOCION_VERSION_NO_EDITABLE",
                    "La versión de promoción no se puede activar en su estado actual."
            );
        }

        if (!hasDiscounts) {
            throw new ConflictException(
                    "PROMOCION_VERSION_SIN_DESCUENTOS",
                    "No se puede activar la versión porque no tiene descuentos por SKU."
            );
        }
    }

    public void validateCanCancel(PromocionVersion version, String motivo) {
        requireActive(version);

        if (version.getEstadoPromocion().isFinalizada()) {
            throw new ConflictException(
                    "PROMOCION_VERSION_FINALIZADA",
                    "La versión de promoción ya se encuentra finalizada o cancelada."
            );
        }

        if (!StringNormalizer.hasText(motivo)) {
            throw new ConflictException(
                    "MOTIVO_CANCELACION_OBLIGATORIO",
                    "Debe indicar el motivo de cancelación."
            );
        }
    }

    public void requireActive(PromocionVersion version) {
        if (version == null) {
            throw new NotFoundException(
                    "PROMOCION_VERSION_NO_ENCONTRADA",
                    "Versión de promoción no encontrada."
            );
        }

        if (!version.isActivo()) {
            throw new NotFoundException(
                    "PROMOCION_VERSION_INACTIVA",
                    "La versión de promoción no está activa."
            );
        }
    }
}