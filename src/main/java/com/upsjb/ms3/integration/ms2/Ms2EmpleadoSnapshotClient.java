// ruta: src/main/java/com/upsjb/ms3/integration/ms2/Ms2EmpleadoSnapshotClient.java
package com.upsjb.ms3.integration.ms2;

import java.time.LocalDateTime;
import java.util.Optional;

public interface Ms2EmpleadoSnapshotClient {

    Optional<EmpleadoSnapshotResponse> findByIdUsuarioMs1(Long idUsuarioMs1);

    Optional<EmpleadoSnapshotResponse> findByIdEmpleadoMs2(Long idEmpleadoMs2);

    EmpleadoSnapshotResponse requestSnapshotByIdUsuarioMs1(Long idUsuarioMs1);

    EmpleadoSnapshotResponse upsertSnapshot(EmpleadoSnapshotRequest request);

    default Optional<EmpleadoSnapshotResponse> obtenerPorIdUsuarioMs1(Long idUsuarioMs1) {
        return findByIdUsuarioMs1(idUsuarioMs1);
    }

    default Optional<EmpleadoSnapshotResponse> obtenerPorIdEmpleadoMs2(Long idEmpleadoMs2) {
        return findByIdEmpleadoMs2(idEmpleadoMs2);
    }

    record EmpleadoSnapshotRequest(
            Long idEmpleadoMs2,
            Long idUsuarioMs1,
            String codigoEmpleado,
            String nombresCompletos,
            String areaCodigo,
            String areaNombre,
            Boolean empleadoActivo,
            Long snapshotVersion,
            LocalDateTime snapshotAt
    ) {
    }

    record EmpleadoSnapshotResponse(
            Long idEmpleadoMs2,
            Long idUsuarioMs1,
            String codigoEmpleado,
            String nombresCompletos,
            String areaCodigo,
            String areaNombre,
            Boolean empleadoActivo,
            Long snapshotVersion,
            LocalDateTime snapshotAt
    ) {

        public boolean isEmpleadoActivo() {
            return Boolean.TRUE.equals(empleadoActivo);
        }
    }
}