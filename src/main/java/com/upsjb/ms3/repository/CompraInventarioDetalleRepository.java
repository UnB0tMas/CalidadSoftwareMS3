// ruta: src/main/java/com/upsjb/ms3/repository/CompraInventarioDetalleRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.CompraInventarioDetalle;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompraInventarioDetalleRepository extends
        JpaRepository<CompraInventarioDetalle, Long>,
        JpaSpecificationExecutor<CompraInventarioDetalle> {

    @EntityGraph(attributePaths = {
            "compra",
            "sku",
            "sku.producto",
            "almacen"
    })
    @Override
    Page<CompraInventarioDetalle> findAll(
            Specification<CompraInventarioDetalle> specification,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "compra",
            "sku",
            "sku.producto",
            "almacen"
    })
    Optional<CompraInventarioDetalle> findByIdCompraDetalleAndEstadoTrue(Long idCompraDetalle);

    boolean existsByCompra_IdCompraAndSku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrue(
            Long idCompra,
            Long idSku,
            Long idAlmacen
    );

    boolean existsByCompra_IdCompraAndSku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrueAndIdCompraDetalleNot(
            Long idCompra,
            Long idSku,
            Long idAlmacen,
            Long idCompraDetalle
    );

    long countByCompra_IdCompraAndEstadoTrue(Long idCompra);

    @EntityGraph(attributePaths = {
            "compra",
            "sku",
            "sku.producto",
            "almacen"
    })
    Page<CompraInventarioDetalle> findByEstadoTrue(Pageable pageable);

    @EntityGraph(attributePaths = {
            "compra",
            "sku",
            "sku.producto",
            "almacen"
    })
    Page<CompraInventarioDetalle> findByCompra_IdCompraAndEstadoTrue(Long idCompra, Pageable pageable);

    @EntityGraph(attributePaths = {
            "compra",
            "sku",
            "sku.producto",
            "almacen"
    })
    Page<CompraInventarioDetalle> findBySku_IdSkuAndEstadoTrue(Long idSku, Pageable pageable);

    @EntityGraph(attributePaths = {
            "compra",
            "sku",
            "sku.producto",
            "almacen"
    })
    Page<CompraInventarioDetalle> findByAlmacen_IdAlmacenAndEstadoTrue(Long idAlmacen, Pageable pageable);

    @EntityGraph(attributePaths = {
            "compra",
            "sku",
            "sku.producto",
            "almacen"
    })
    List<CompraInventarioDetalle> findByCompra_IdCompraAndEstadoTrueOrderByIdCompraDetalleAsc(Long idCompra);

    @Query("""
            select coalesce(sum(d.costoTotal), 0)
            from CompraInventarioDetalle d
            where d.compra.idCompra = :idCompra
              and d.estado = true
            """)
    BigDecimal sumCostoTotalByCompra(@Param("idCompra") Long idCompra);

    @Query("""
            select coalesce(sum(d.cantidad), 0)
            from CompraInventarioDetalle d
            where d.compra.idCompra = :idCompra
              and d.estado = true
            """)
    Long sumCantidadByCompra(@Param("idCompra") Long idCompra);
}