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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompraInventarioRepository extends
        JpaRepository<CompraInventario, Long>,
        JpaSpecificationExecutor<CompraInventario> {

    Optional<CompraInventario> findByIdCompraAndEstadoTrue(Long idCompra);

    Optional<CompraInventario> findByCodigoCompraIgnoreCaseAndEstadoTrue(String codigoCompra);

    boolean existsByCodigoCompraIgnoreCaseAndEstadoTrue(String codigoCompra);

    boolean existsByCodigoCompraIgnoreCaseAndEstadoTrueAndIdCompraNot(String codigoCompra, Long idCompra);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from CompraInventario c
            where c.idCompra = :idCompra
              and c.estado = true
            """)
    Optional<CompraInventario> findActivoByIdForUpdate(@Param("idCompra") Long idCompra);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from CompraInventario c
            where upper(c.codigoCompra) = upper(:codigoCompra)
              and c.estado = true
            """)
    Optional<CompraInventario> findActivoByCodigoForUpdate(@Param("codigoCompra") String codigoCompra);

    Page<CompraInventario> findByEstadoTrue(Pageable pageable);

    Page<CompraInventario> findByProveedor_IdProveedorAndEstadoTrue(Long idProveedor, Pageable pageable);

    Page<CompraInventario> findByEstadoCompraAndEstadoTrue(
            EstadoCompraInventario estadoCompra,
            Pageable pageable
    );

    Page<CompraInventario> findByCreadoPorIdUsuarioMs1AndEstadoTrue(
            Long creadoPorIdUsuarioMs1,
            Pageable pageable
    );

    List<CompraInventario> findByEstadoCompraAndEstadoTrueOrderByFechaCompraDescIdCompraDesc(
            EstadoCompraInventario estadoCompra
    );

    List<CompraInventario> findByFechaCompraBetweenAndEstadoTrueOrderByFechaCompraDescIdCompraDesc(
            LocalDateTime desde,
            LocalDateTime hasta
    );
}