// ruta: src/main/java/com/upsjb/ms3/repository/MovimientoInventarioRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.MovimientoInventario;
import com.upsjb.ms3.domain.enums.EstadoMovimientoInventario;
import com.upsjb.ms3.domain.enums.MotivoMovimientoInventario;
import com.upsjb.ms3.domain.enums.TipoMovimientoInventario;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface MovimientoInventarioRepository extends
        JpaRepository<MovimientoInventario, Long>,
        JpaSpecificationExecutor<MovimientoInventario> {

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto",
            "almacen",
            "compraDetalle",
            "reservaStock"
    })
    @Override
    Page<MovimientoInventario> findAll(Specification<MovimientoInventario> specification, Pageable pageable);

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto",
            "almacen",
            "compraDetalle",
            "reservaStock"
    })
    Optional<MovimientoInventario> findByIdMovimientoAndEstadoTrue(Long idMovimiento);

    Optional<MovimientoInventario> findByCodigoMovimientoIgnoreCaseAndEstadoTrue(String codigoMovimiento);

    boolean existsByCodigoMovimientoIgnoreCaseAndEstadoTrue(String codigoMovimiento);

    boolean existsByAlmacen_IdAlmacenAndEstadoTrue(Long idAlmacen);

    boolean existsByReferenciaTipoAndReferenciaIdExternoAndTipoMovimientoAndEstadoTrue(
            String referenciaTipo,
            String referenciaIdExterno,
            TipoMovimientoInventario tipoMovimiento
    );

    boolean existsByReferenciaTipoAndReferenciaIdExternoAndTipoMovimientoAndSku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrue(
            String referenciaTipo,
            String referenciaIdExterno,
            TipoMovimientoInventario tipoMovimiento,
            Long idSku,
            Long idAlmacen
    );

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto",
            "almacen",
            "compraDetalle",
            "reservaStock"
    })
    Page<MovimientoInventario> findByEstadoTrue(Pageable pageable);

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto",
            "almacen",
            "compraDetalle",
            "reservaStock"
    })
    Page<MovimientoInventario> findBySku_IdSkuAndEstadoTrue(Long idSku, Pageable pageable);

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto",
            "almacen",
            "compraDetalle",
            "reservaStock"
    })
    Page<MovimientoInventario> findByAlmacen_IdAlmacenAndEstadoTrue(Long idAlmacen, Pageable pageable);

    Page<MovimientoInventario> findByTipoMovimientoAndEstadoTrue(
            TipoMovimientoInventario tipoMovimiento,
            Pageable pageable
    );

    Page<MovimientoInventario> findByMotivoMovimientoAndEstadoTrue(
            MotivoMovimientoInventario motivoMovimiento,
            Pageable pageable
    );

    Page<MovimientoInventario> findByEstadoMovimientoAndEstadoTrue(
            EstadoMovimientoInventario estadoMovimiento,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto",
            "almacen",
            "compraDetalle",
            "reservaStock"
    })
    Page<MovimientoInventario> findByReferenciaTipoAndReferenciaIdExternoAndEstadoTrue(
            String referenciaTipo,
            String referenciaIdExterno,
            Pageable pageable
    );

    List<MovimientoInventario> findByReservaStock_IdReservaStockAndEstadoTrueOrderByCreatedAtAscIdMovimientoAsc(
            Long idReservaStock
    );

    List<MovimientoInventario> findByCompraDetalle_IdCompraDetalleAndEstadoTrueOrderByCreatedAtAscIdMovimientoAsc(
            Long idCompraDetalle
    );

    List<MovimientoInventario> findBySku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrueOrderByCreatedAtDescIdMovimientoDesc(
            Long idSku,
            Long idAlmacen
    );

    List<MovimientoInventario> findBySku_IdSkuAndAlmacen_IdAlmacenAndTipoMovimientoInAndEstadoTrueOrderByCreatedAtDescIdMovimientoDesc(
            Long idSku,
            Long idAlmacen,
            Collection<TipoMovimientoInventario> tiposMovimiento
    );

    List<MovimientoInventario> findByCreatedAtBetweenAndEstadoTrueOrderByCreatedAtDescIdMovimientoDesc(
            LocalDateTime desde,
            LocalDateTime hasta
    );
}