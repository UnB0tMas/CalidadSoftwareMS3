// ruta: src/main/java/com/upsjb/ms3/repository/CompraInventarioRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.CompraInventario;
import com.upsjb.ms3.domain.enums.EstadoCompraInventario;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompraInventarioRepository extends
        JpaRepository<CompraInventario, Long>,
        JpaSpecificationExecutor<CompraInventario> {

    @EntityGraph(attributePaths = {
            "proveedor"
    })
    @Override
    Page<CompraInventario> findAll(Specification<CompraInventario> specification, Pageable pageable);

    @EntityGraph(attributePaths = {
            "proveedor"
    })
    Optional<CompraInventario> findByIdCompraAndEstadoTrue(Long idCompra);

    @EntityGraph(attributePaths = {
            "proveedor"
    })
    Optional<CompraInventario> findByCodigoCompraIgnoreCaseAndEstadoTrue(String codigoCompra);

    boolean existsByCodigoCompraIgnoreCaseAndEstadoTrue(String codigoCompra);

    boolean existsByCodigoCompraIgnoreCaseAndEstadoTrueAndIdCompraNot(String codigoCompra, Long idCompra);

    boolean existsByProveedor_IdProveedorAndEstadoCompraAndEstadoTrue(
            Long idProveedor,
            EstadoCompraInventario estadoCompra
    );

    Long countByProveedor_IdProveedorAndEstadoTrue(Long idProveedor);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "proveedor"
    })
    @Query("""
            select c
            from CompraInventario c
            where c.idCompra = :idCompra
              and c.estado = true
            """)
    Optional<CompraInventario> findActivoByIdForUpdate(@Param("idCompra") Long idCompra);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "proveedor"
    })
    @Query("""
            select c
            from CompraInventario c
            where upper(c.codigoCompra) = upper(:codigoCompra)
              and c.estado = true
            """)
    Optional<CompraInventario> findActivoByCodigoForUpdate(@Param("codigoCompra") String codigoCompra);

    @EntityGraph(attributePaths = {
            "proveedor"
    })
    Page<CompraInventario> findByEstadoTrue(Pageable pageable);

    @EntityGraph(attributePaths = {
            "proveedor"
    })
    Page<CompraInventario> findByProveedor_IdProveedorAndEstadoTrue(Long idProveedor, Pageable pageable);

    @EntityGraph(attributePaths = {
            "proveedor"
    })
    Page<CompraInventario> findByEstadoCompraAndEstadoTrue(
            EstadoCompraInventario estadoCompra,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "proveedor"
    })
    Page<CompraInventario> findByCreadoPorIdUsuarioMs1AndEstadoTrue(
            Long creadoPorIdUsuarioMs1,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "proveedor"
    })
    List<CompraInventario> findByEstadoCompraAndEstadoTrueOrderByFechaCompraDescIdCompraDesc(
            EstadoCompraInventario estadoCompra
    );

    @EntityGraph(attributePaths = {
            "proveedor"
    })
    List<CompraInventario> findByFechaCompraBetweenAndEstadoTrueOrderByFechaCompraDescIdCompraDesc(
            LocalDateTime desde,
            LocalDateTime hasta
    );
}