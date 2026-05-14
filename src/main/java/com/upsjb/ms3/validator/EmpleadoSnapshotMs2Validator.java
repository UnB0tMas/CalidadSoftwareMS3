// ruta: src/main/java/com/upsjb/ms3/validator/EmpleadoSnapshotMs2Validator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.EmpleadoSnapshotMs2;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class EmpleadoSnapshotMs2Validator {

    public void validateUpsert(
            Long idEmpleadoMs2,
            Long idUsuarioMs1,
            String codigoEmpleado,
            String nombresCompletos,
            Boolean empleadoActivo,
            LocalDateTime snapshotAt,
            boolean duplicatedByEmpleado,
            boolean duplicatedByUsuario,
            boolean duplicatedByCodigo,
            boolean staleSnapshotVersion
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (idEmpleadoMs2 == null) {
            errors.add("idEmpleadoMs2", "El id del empleado MS2 es obligatorio.", "REQUIRED", null);
        }

        if (idUsuarioMs1 == null) {
            errors.add("idUsuarioMs1", "El id de usuario MS1 es obligatorio.", "REQUIRED", null);
        }

        if (!StringNormalizer.hasText(codigoEmpleado)) {
            errors.add("codigoEmpleado", "El código del empleado es obligatorio.", "REQUIRED", codigoEmpleado);
        }

        if (!StringNormalizer.hasText(nombresCompletos)) {
            errors.add("nombresCompletos", "Los nombres completos del empleado son obligatorios.", "REQUIRED", nombresCompletos);
        }

        if (empleadoActivo == null) {
            errors.add("empleadoActivo", "Debe indicar si el empleado está activo en MS2.", "REQUIRED", null);
        }

        if (snapshotAt == null) {
            errors.add("snapshotAt", "La fecha del snapshot es obligatoria.", "REQUIRED", null);
        }

        errors.throwIfAny("No se puede registrar el snapshot del empleado MS2.");

        if (duplicatedByEmpleado) {
            throw new ConflictException(
                    "EMPLEADO_MS2_DUPLICADO",
                    "Ya existe otro snapshot activo para el empleado MS2 indicado."
            );
        }

        if (duplicatedByUsuario) {
            throw new ConflictException(
                    "USUARIO_MS1_EMPLEADO_DUPLICADO",
                    "Ya existe otro snapshot activo para el usuario MS1 indicado."
            );
        }

        if (duplicatedByCodigo) {
            throw new ConflictException(
                    "CODIGO_EMPLEADO_DUPLICADO",
                    "Ya existe otro snapshot activo con el código de empleado indicado."
            );
        }

        if (staleSnapshotVersion) {
            throw new ConflictException(
                    "SNAPSHOT_EMPLEADO_OBSOLETO",
                    "No se puede reemplazar el snapshot porque la versión recibida es anterior a la versión vigente."
            );
        }
    }

    public void requireActiveEmployee(EmpleadoSnapshotMs2 empleado) {
        if (empleado == null) {
            throw new NotFoundException(
                    "EMPLEADO_SNAPSHOT_NO_ENCONTRADO",
                    "Snapshot de empleado MS2 no encontrado."
            );
        }

        if (!empleado.isActivo() || !Boolean.TRUE.equals(empleado.getEmpleadoActivo())) {
            throw new ConflictException(
                    "EMPLEADO_MS2_INACTIVO",
                    "El empleado no está activo para operar inventario."
            );
        }
    }

    public void requireReference(Long idEmpleadoMs2, Long idUsuarioMs1, String codigoEmpleado) {
        if (idEmpleadoMs2 == null
                && idUsuarioMs1 == null
                && !StringNormalizer.hasText(codigoEmpleado)) {
            throw new NotFoundException(
                    "EMPLEADO_REFERENCIA_OBLIGATORIA",
                    "Debe indicar el empleado por idEmpleadoMs2, idUsuarioMs1 o código de empleado."
            );
        }
    }
}