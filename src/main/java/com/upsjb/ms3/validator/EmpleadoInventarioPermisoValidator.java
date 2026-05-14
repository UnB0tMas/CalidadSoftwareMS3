// ruta: src/main/java/com/upsjb/ms3/validator/EmpleadoInventarioPermisoValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.EmpleadoInventarioPermisoHistorial;
import com.upsjb.ms3.domain.entity.EmpleadoSnapshotMs2;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class EmpleadoInventarioPermisoValidator {

    public void validateGrant(
            EmpleadoSnapshotMs2 empleadoSnapshot,
            LocalDateTime fechaInicio,
            String motivo,
            Long otorgadoPorIdUsuarioMs1,
            boolean alreadyHasCurrentPermission,
            boolean selfGrant
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateEmpleadoSnapshot(errors, empleadoSnapshot);

        if (fechaInicio == null) {
            errors.add("fechaInicio", "La fecha de inicio del permiso es obligatoria.", "REQUIRED", null);
        }

        if (!StringNormalizer.hasText(motivo)) {
            errors.add("motivo", "El motivo del permiso es obligatorio.", "REQUIRED", motivo);
        }

        if (otorgadoPorIdUsuarioMs1 == null) {
            errors.add("otorgadoPorIdUsuarioMs1", "El usuario que otorga el permiso es obligatorio.", "REQUIRED", null);
        }

        errors.throwIfAny("No se puede otorgar el permiso de inventario.");

        if (alreadyHasCurrentPermission) {
            throw new ConflictException(
                    "EMPLEADO_PERMISO_VIGENTE_EXISTENTE",
                    "El empleado ya tiene permisos de inventario vigentes."
            );
        }

        if (selfGrant) {
            throw new ConflictException(
                    "EMPLEADO_NO_PUEDE_AUTORIZARSE",
                    "Un empleado no puede otorgarse permisos de inventario a sí mismo."
            );
        }
    }

    public void validateGrantOrReplace(
            EmpleadoSnapshotMs2 empleadoSnapshot,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            String motivo,
            Long otorgadoPorIdUsuarioMs1,
            boolean selfGrant
    ) {
        validateGrant(
                empleadoSnapshot,
                fechaInicio,
                motivo,
                otorgadoPorIdUsuarioMs1,
                false,
                selfGrant
        );
        validateDateRange(fechaInicio, fechaFin);
    }

    public void validatePermissionPayload(EmpleadoInventarioPermisoHistorial permiso) {
        if (permiso == null) {
            throw new ConflictException(
                    "PERMISO_INVENTARIO_INVALIDO",
                    "El permiso de inventario no tiene datos válidos."
            );
        }

        validateDateRange(permiso.getFechaInicio(), permiso.getFechaFin());

        boolean hasAnyPermission = Boolean.TRUE.equals(permiso.getPuedeCrearProductoBasico())
                || Boolean.TRUE.equals(permiso.getPuedeEditarProductoBasico())
                || Boolean.TRUE.equals(permiso.getPuedeRegistrarEntrada())
                || Boolean.TRUE.equals(permiso.getPuedeRegistrarSalida())
                || Boolean.TRUE.equals(permiso.getPuedeRegistrarAjuste())
                || Boolean.TRUE.equals(permiso.getPuedeConsultarKardex())
                || Boolean.TRUE.equals(permiso.getPuedeGestionarImagenes())
                || Boolean.TRUE.equals(permiso.getPuedeActualizarAtributos());

        if (!hasAnyPermission) {
            throw new ConflictException(
                    "PERMISO_INVENTARIO_SIN_PERMISOS",
                    "Debe activar al menos un permiso funcional de inventario."
            );
        }
    }

    public void validateRevoke(
            EmpleadoInventarioPermisoHistorial permiso,
            String motivo,
            Long revocadoPorIdUsuarioMs1
    ) {
        requireCurrent(permiso);

        if (!StringNormalizer.hasText(motivo)) {
            throw new ConflictException(
                    "MOTIVO_REVOCACION_OBLIGATORIO",
                    "Debe indicar el motivo de revocación del permiso."
            );
        }

        if (revocadoPorIdUsuarioMs1 == null) {
            throw new ConflictException(
                    "USUARIO_REVOCACION_OBLIGATORIO",
                    "El usuario que revoca el permiso es obligatorio."
            );
        }
    }

    public void requireCurrent(EmpleadoInventarioPermisoHistorial permiso) {
        if (permiso == null) {
            throw new NotFoundException(
                    "PERMISO_INVENTARIO_NO_ENCONTRADO",
                    "Permiso de inventario no encontrado."
            );
        }

        if (!permiso.isActivo() || !Boolean.TRUE.equals(permiso.getVigente())) {
            throw new ConflictException(
                    "PERMISO_INVENTARIO_NO_VIGENTE",
                    "El permiso de inventario no está vigente."
            );
        }
    }

    private void validateEmpleadoSnapshot(
            ValidationErrorCollector errors,
            EmpleadoSnapshotMs2 empleadoSnapshot
    ) {
        if (empleadoSnapshot == null) {
            errors.add("empleadoSnapshot", "El empleado es obligatorio.", "REQUIRED", null);
            return;
        }

        if (!empleadoSnapshot.isActivo() || !Boolean.TRUE.equals(empleadoSnapshot.getEmpleadoActivo())) {
            errors.add(
                    "empleadoSnapshot",
                    "El empleado debe estar activo.",
                    "INACTIVE",
                    empleadoSnapshot.getIdEmpleadoSnapshot()
            );
        }
    }

    private void validateDateRange(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new ConflictException(
                    "PERMISO_FECHA_FIN_INVALIDA",
                    "La fecha fin del permiso no puede ser menor que la fecha inicio."
            );
        }
    }
}