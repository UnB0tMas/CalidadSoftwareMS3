// ruta: src/main/java/com/upsjb/ms3/repository/EmpleadoInventarioPermisoHistorialRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.EmpleadoInventarioPermisoHistorial;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpleadoInventarioPermisoHistorialRepository extends
        JpaRepository<EmpleadoInventarioPermisoHistorial, Long>,
        JpaSpecificationExecutor<EmpleadoInventarioPermisoHistorial> {

    @EntityGraph(attributePaths = {
            "empleadoSnapshot"
    })
    @Override
    Page<EmpleadoInventarioPermisoHistorial> findAll(
            Specification<EmpleadoInventarioPermisoHistorial> specification,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "empleadoSnapshot"
    })
    Optional<EmpleadoInventarioPermisoHistorial> findByIdPermisoHistorialAndEstadoTrue(Long idPermisoHistorial);

    @EntityGraph(attributePaths = {
            "empleadoSnapshot"
    })
    Optional<EmpleadoInventarioPermisoHistorial>
    findFirstByEmpleadoSnapshot_IdEmpleadoSnapshotAndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPermisoHistorialDesc(
            Long idEmpleadoSnapshot
    );

    @EntityGraph(attributePaths = {
            "empleadoSnapshot"
    })
    Optional<EmpleadoInventarioPermisoHistorial>
    findFirstByEmpleadoSnapshot_IdEmpleadoMs2AndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPermisoHistorialDesc(
            Long idEmpleadoMs2
    );

    @EntityGraph(attributePaths = {
            "empleadoSnapshot"
    })
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

    @EntityGraph(attributePaths = {
            "empleadoSnapshot"
    })
    Page<EmpleadoInventarioPermisoHistorial> findByEstadoTrue(Pageable pageable);

    @EntityGraph(attributePaths = {
            "empleadoSnapshot"
    })
    Page<EmpleadoInventarioPermisoHistorial> findByVigenteAndEstadoTrue(Boolean vigente, Pageable pageable);

    @EntityGraph(attributePaths = {
            "empleadoSnapshot"
    })
    Page<EmpleadoInventarioPermisoHistorial> findByEmpleadoSnapshot_IdEmpleadoSnapshotAndEstadoTrue(
            Long idEmpleadoSnapshot,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "empleadoSnapshot"
    })
    Page<EmpleadoInventarioPermisoHistorial> findByEmpleadoSnapshot_IdEmpleadoMs2AndEstadoTrue(
            Long idEmpleadoMs2,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "empleadoSnapshot"
    })
    Page<EmpleadoInventarioPermisoHistorial> findByEmpleadoSnapshot_IdUsuarioMs1AndEstadoTrue(
            Long idUsuarioMs1,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "empleadoSnapshot"
    })
    List<EmpleadoInventarioPermisoHistorial>
    findByVigenteTrueAndEstadoTrueAndFechaFinBeforeOrderByFechaFinAsc(LocalDateTime fechaFin);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "empleadoSnapshot"
    })
    @Query("""
            select p
            from EmpleadoInventarioPermisoHistorial p
            where p.idPermisoHistorial = :idPermisoHistorial
              and p.estado = true
            """)
    Optional<EmpleadoInventarioPermisoHistorial> findActivoByIdForUpdate(
            @Param("idPermisoHistorial") Long idPermisoHistorial
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "empleadoSnapshot"
    })
    @Query("""
            select p
            from EmpleadoInventarioPermisoHistorial p
            where p.empleadoSnapshot.idEmpleadoSnapshot = :idEmpleadoSnapshot
              and p.vigente = true
              and p.estado = true
            order by p.fechaInicio desc, p.idPermisoHistorial desc
            """)
    List<EmpleadoInventarioPermisoHistorial> findVigentesByEmpleadoSnapshotForUpdate(
            @Param("idEmpleadoSnapshot") Long idEmpleadoSnapshot,
            Pageable pageable
    );
}