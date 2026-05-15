// ruta: src/main/java/com/upsjb/ms3/validator/PromocionVersionValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Promocion;
import com.upsjb.ms3.domain.entity.PromocionVersion;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.DateTimeUtil;
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
        } else if (estadoPromocion == EstadoPromocion.FINALIZADA || estadoPromocion == EstadoPromocion.CANCELADA) {
            errors.add(
                    "estadoPromocion",
                    "Una nueva versión de promoción no puede crearse finalizada ni cancelada.",
                    "INVALID_STATE",
                    estadoPromocion
            );
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

    public void validateCanChangeState(
            PromocionVersion version,
            EstadoPromocion targetState,
            String motivo,
            boolean hasDiscounts
    ) {
        validateCanChangeState(version, targetState, motivo, hasDiscounts, DateTimeUtil.nowUtc());
    }

    public void validateCanChangeState(
            PromocionVersion version,
            EstadoPromocion targetState,
            String motivo,
            boolean hasDiscounts,
            LocalDateTime now
    ) {
        requireActive(version);

        if (targetState == null) {
            throw new ConflictException(
                    "PROMOCION_ESTADO_REQUERIDO",
                    "Debe indicar el estado de promoción."
            );
        }

        if (!StringNormalizer.hasText(motivo)) {
            throw new ConflictException(
                    "MOTIVO_OBLIGATORIO",
                    "Debe indicar el motivo de la operación."
            );
        }

        if (version.getEstadoPromocion() != null && version.getEstadoPromocion().isFinalizada()) {
            throw new ConflictException(
                    "PROMOCION_VERSION_FINALIZADA",
                    "La versión de promoción ya se encuentra finalizada o cancelada."
            );
        }

        if (targetState == EstadoPromocion.ACTIVA || targetState == EstadoPromocion.PROGRAMADA) {
            validateCanActivate(version, targetState, hasDiscounts, now);
            return;
        }

        if (targetState == EstadoPromocion.CANCELADA) {
            validateCanCancel(version, motivo);
            return;
        }

        if (targetState == EstadoPromocion.FINALIZADA) {
            validateCanFinalize(version);
            return;
        }

        if (targetState == EstadoPromocion.BORRADOR && version.getEstadoPromocion() == EstadoPromocion.ACTIVA) {
            throw new ConflictException(
                    "PROMOCION_ACTIVA_NO_REGRESA_BORRADOR",
                    "No se puede regresar una promoción activa a borrador."
            );
        }
    }

    public void validateCanActivate(PromocionVersion version, boolean hasDiscounts) {
        validateCanActivate(version, EstadoPromocion.ACTIVA, hasDiscounts, DateTimeUtil.nowUtc());
    }

    public void validateCanActivate(
            PromocionVersion version,
            EstadoPromocion targetState,
            boolean hasDiscounts,
            LocalDateTime now
    ) {
        requireActive(version);

        if (targetState != EstadoPromocion.ACTIVA && targetState != EstadoPromocion.PROGRAMADA) {
            throw new ConflictException(
                    "PROMOCION_ESTADO_PUBLICACION_INVALIDO",
                    "La versión solo puede activarse o programarse."
            );
        }

        if (version.getEstadoPromocion() != null && version.getEstadoPromocion().isFinalizada()) {
            throw new ConflictException(
                    "PROMOCION_VERSION_FINALIZADA",
                    "La versión de promoción ya se encuentra finalizada o cancelada."
            );
        }

        if (!hasDiscounts) {
            throw new ConflictException(
                    "PROMOCION_VERSION_SIN_DESCUENTOS",
                    "No se puede activar la versión porque no tiene descuentos por SKU."
            );
        }

        LocalDateTime resolvedNow = now == null ? DateTimeUtil.nowUtc() : now;

        if (version.getFechaFin() != null && version.getFechaFin().isBefore(resolvedNow)) {
            throw new ConflictException(
                    "PROMOCION_VERSION_VENCIDA",
                    "No se puede activar la promoción porque la fecha fin ya venció."
            );
        }

        if (targetState == EstadoPromocion.ACTIVA
                && version.getFechaInicio() != null
                && version.getFechaInicio().isAfter(resolvedNow)) {
            throw new ConflictException(
                    "PROMOCION_VERSION_FECHA_FUTURA",
                    "No se puede activar la promoción porque su fecha de inicio es futura. Prográmela."
            );
        }
    }

    public void validateCanFinalize(PromocionVersion version) {
        requireActive(version);

        if (version.getEstadoPromocion() != null && version.getEstadoPromocion().isFinalizada()) {
            throw new ConflictException(
                    "PROMOCION_VERSION_FINALIZADA",
                    "La versión de promoción ya se encuentra finalizada o cancelada."
            );
        }
    }

    public void validateCanCancel(PromocionVersion version, String motivo) {
        requireActive(version);

        if (version.getEstadoPromocion() != null && version.getEstadoPromocion().isFinalizada()) {
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