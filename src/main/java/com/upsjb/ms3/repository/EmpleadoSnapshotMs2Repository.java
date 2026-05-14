// ruta: src/main/java/com/upsjb/ms3/repository/EmpleadoSnapshotMs2Repository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.EmpleadoSnapshotMs2;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmpleadoSnapshotMs2Repository extends
        JpaRepository<EmpleadoSnapshotMs2, Long>,
        JpaSpecificationExecutor<EmpleadoSnapshotMs2> {

    Optional<EmpleadoSnapshotMs2> findByIdEmpleadoSnapshotAndEstadoTrue(Long idEmpleadoSnapshot);

    Optional<EmpleadoSnapshotMs2> findByIdEmpleadoMs2AndEstadoTrue(Long idEmpleadoMs2);

    Optional<EmpleadoSnapshotMs2> findByIdUsuarioMs1AndEstadoTrue(Long idUsuarioMs1);

    Optional<EmpleadoSnapshotMs2> findByCodigoEmpleadoIgnoreCaseAndEstadoTrue(String codigoEmpleado);

    boolean existsByIdEmpleadoMs2AndEstadoTrue(Long idEmpleadoMs2);

    boolean existsByIdUsuarioMs1AndEstadoTrue(Long idUsuarioMs1);

    boolean existsByCodigoEmpleadoIgnoreCaseAndEstadoTrue(String codigoEmpleado);

    boolean existsByIdEmpleadoMs2AndEstadoTrueAndIdEmpleadoSnapshotNot(Long idEmpleadoMs2, Long idEmpleadoSnapshot);

    boolean existsByIdUsuarioMs1AndEstadoTrueAndIdEmpleadoSnapshotNot(Long idUsuarioMs1, Long idEmpleadoSnapshot);

    boolean existsByCodigoEmpleadoIgnoreCaseAndEstadoTrueAndIdEmpleadoSnapshotNot(
            String codigoEmpleado,
            Long idEmpleadoSnapshot
    );

    Page<EmpleadoSnapshotMs2> findByEstadoTrue(Pageable pageable);

    Page<EmpleadoSnapshotMs2> findByEmpleadoActivoAndEstadoTrue(Boolean empleadoActivo, Pageable pageable);

    List<EmpleadoSnapshotMs2> findByEmpleadoActivoTrueAndEstadoTrueOrderByNombresCompletosAsc();

    List<EmpleadoSnapshotMs2> findByAreaCodigoIgnoreCaseAndEstadoTrueOrderByNombresCompletosAsc(String areaCodigo);
}