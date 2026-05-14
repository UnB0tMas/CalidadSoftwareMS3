// ruta: src/main/java/com/upsjb/ms3/repository/PrecioSkuHistorialRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.PrecioSkuHistorial;
import com.upsjb.ms3.domain.enums.Moneda;
import java.time.LocalDateTime;
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

public interface PrecioSkuHistorialRepository extends
        JpaRepository<PrecioSkuHistorial, Long>,
        JpaSpecificationExecutor<PrecioSkuHistorial> {

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto"
    })
    @Override
    Page<PrecioSkuHistorial> findAll(Specification<PrecioSkuHistorial> specification, Pageable pageable);

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto"
    })
    Optional<PrecioSkuHistorial> findByIdPrecioHistorialAndEstadoTrue(Long idPrecioHistorial);

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto"
    })
    Optional<PrecioSkuHistorial>
    findFirstBySku_IdSkuAndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPrecioHistorialDesc(Long idSku);

    boolean existsBySku_IdSkuAndVigenteTrueAndEstadoTrue(Long idSku);

    boolean existsBySku_IdSkuAndVigenteTrueAndEstadoTrueAndIdPrecioHistorialNot(
            Long idSku,
            Long idPrecioHistorial
    );

    boolean existsBySku_IdSkuAndFechaInicioAndEstadoTrue(
            Long idSku,
            LocalDateTime fechaInicio
    );

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto"
    })
    Page<PrecioSkuHistorial> findByEstadoTrue(Pageable pageable);

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto"
    })
    Page<PrecioSkuHistorial> findBySku_IdSkuAndEstadoTrue(Long idSku, Pageable pageable);

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto"
    })
    Page<PrecioSkuHistorial> findBySku_IdSkuAndMonedaAndEstadoTrue(Long idSku, Moneda moneda, Pageable pageable);

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto"
    })
    List<PrecioSkuHistorial> findBySku_IdSkuAndEstadoTrueOrderByFechaInicioDescIdPrecioHistorialDesc(Long idSku);

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto"
    })
    @Query("""
            select p
            from PrecioSkuHistorial p
            where p.sku.idSku = :idSku
              and p.estado = true
              and p.fechaInicio <= :fecha
              and (p.fechaFin is null or p.fechaFin >= :fecha)
            order by p.fechaInicio desc, p.idPrecioHistorial desc
            """)
    List<PrecioSkuHistorial> findPreciosAplicablesBySkuAndFecha(
            @Param("idSku") Long idSku,
            @Param("fecha") LocalDateTime fecha
    );

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto"
    })
    @Query("""
            select p
            from PrecioSkuHistorial p
            where p.vigente = true
              and p.estado = true
              and p.fechaInicio <= :fecha
              and (p.fechaFin is null or p.fechaFin >= :fecha)
            order by p.fechaInicio desc, p.idPrecioHistorial desc
            """)
    List<PrecioSkuHistorial> findPreciosVigentesAt(@Param("fecha") LocalDateTime fecha);
}