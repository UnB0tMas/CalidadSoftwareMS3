// ruta: src/main/java/com/upsjb/ms3/repository/EmpleadoInventarioPermisoHistorialRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.EmpleadoInventarioPermisoHistorial;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmpleadoInventarioPermisoHistorialRepository extends
        JpaRepository<EmpleadoInventarioPermisoHistorial, Long>,
        JpaSpecificationExecutor<EmpleadoInventarioPermisoHistorial> {

    Optional<EmpleadoInventarioPermisoHistorial> findByIdPermisoHistorialAndEstadoTrue(Long idPermisoHistorial);

    Optional<EmpleadoInventarioPermisoHistorial>
    findFirstByEmpleadoSnapshot_IdEmpleadoSnapshotAndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPermisoHistorialDesc(
            Long idEmpleadoSnapshot
    );

    Optional<EmpleadoInventarioPermisoHistorial>
    findFirstByEmpleadoSnapshot_IdEmpleadoMs2AndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPermisoHistorialDesc(
            Long idEmpleadoMs2
    );

    Optional<EmpleadoInventarioPermisoHistorial>
    findFirstByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPermisoHistorialDesc(
            Long idUsuarioMs1
    );

    boolean existsByEmpleadoSnapshot_IdEmpleadoSnapshotAndVigenteTrueAndEstadoTrue(Long idEmpleadoSnapshot);

    boolean existsByEmpleadoSnapshot_IdEmpleadoMs2AndVigenteTrueAndEstadoTrue(Long idEmpleadoMs2);

    boolean existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrue(Long idUsuarioMs1);

    boolean existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueAndPuedeCrearProductoBasicoTrue(
            Long idUsuarioMs1
    );

    boolean existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueAndPuedeEditarProductoBasicoTrue(
            Long idUsuarioMs1
    );

    boolean existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueAndPuedeRegistrarEntradaTrue(
            Long idUsuarioMs1
    );

    boolean existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueAndPuedeRegistrarSalidaTrue(
            Long idUsuarioMs1
    );

    boolean existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueAndPuedeRegistrarAjusteTrue(
            Long idUsuarioMs1
    );

    boolean existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueAndPuedeConsultarKardexTrue(
            Long idUsuarioMs1
    );

    boolean existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueAndPuedeGestionarImagenesTrue(
            Long idUsuarioMs1
    );

    boolean existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueAndPuedeActualizarAtributosTrue(
            Long idUsuarioMs1
    );

    Page<EmpleadoInventarioPermisoHistorial> findByEstadoTrue(Pageable pageable);

    Page<EmpleadoInventarioPermisoHistorial> findByVigenteAndEstadoTrue(Boolean vigente, Pageable pageable);

    Page<EmpleadoInventarioPermisoHistorial> findByEmpleadoSnapshot_IdEmpleadoSnapshotAndEstadoTrue(
            Long idEmpleadoSnapshot,
            Pageable pageable
    );

    Page<EmpleadoInventarioPermisoHistorial> findByEmpleadoSnapshot_IdEmpleadoMs2AndEstadoTrue(
            Long idEmpleadoMs2,
            Pageable pageable
    );

    Page<EmpleadoInventarioPermisoHistorial> findByEmpleadoSnapshot_IdUsuarioMs1AndEstadoTrue(
            Long idUsuarioMs1,
            Pageable pageable
    );

    List<EmpleadoInventarioPermisoHistorial>
    findByVigenteTrueAndEstadoTrueAndFechaFinBeforeOrderByFechaFinAsc(LocalDateTime fechaFin);
}