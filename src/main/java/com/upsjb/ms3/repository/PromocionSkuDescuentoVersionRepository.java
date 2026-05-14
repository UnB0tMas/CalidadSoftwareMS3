// ruta: src/main/java/com/upsjb/ms3/repository/PromocionSkuDescuentoVersionRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.PromocionSkuDescuentoVersion;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.domain.enums.TipoDescuento;
import java.time.LocalDateTime;
import java.util.Collection;
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

public interface PromocionSkuDescuentoVersionRepository extends
        JpaRepository<PromocionSkuDescuentoVersion, Long>,
        JpaSpecificationExecutor<PromocionSkuDescuentoVersion> {

    @EntityGraph(attributePaths = {
            "promocionVersion",
            "promocionVersion.promocion",
            "sku",
            "sku.producto"
    })
    @Override
    Page<PromocionSkuDescuentoVersion> findAll(
            Specification<PromocionSkuDescuentoVersion> specification,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "promocionVersion",
            "promocionVersion.promocion",
            "sku",
            "sku.producto"
    })
    Optional<PromocionSkuDescuentoVersion> findByIdPromocionSkuDescuentoVersionAndEstadoTrue(
            Long idPromocionSkuDescuentoVersion
    );

    @EntityGraph(attributePaths = {
            "promocionVersion",
            "promocionVersion.promocion",
            "sku",
            "sku.producto"
    })
    Optional<PromocionSkuDescuentoVersion>
    findByPromocionVersion_IdPromocionVersionAndSku_IdSkuAndEstadoTrue(
            Long idPromocionVersion,
            Long idSku
    );

    boolean existsByPromocionVersion_IdPromocionVersionAndSku_IdSkuAndEstadoTrue(
            Long idPromocionVersion,
            Long idSku
    );

    boolean existsByPromocionVersion_IdPromocionVersionAndSku_IdSkuAndEstadoTrueAndIdPromocionSkuDescuentoVersionNot(
            Long idPromocionVersion,
            Long idSku,
            Long idPromocionSkuDescuentoVersion
    );

    @EntityGraph(attributePaths = {
            "promocionVersion",
            "promocionVersion.promocion",
            "sku",
            "sku.producto"
    })
    Page<PromocionSkuDescuentoVersion> findByEstadoTrue(Pageable pageable);

    @EntityGraph(attributePaths = {
            "promocionVersion",
            "promocionVersion.promocion",
            "sku",
            "sku.producto"
    })
    Page<PromocionSkuDescuentoVersion> findByPromocionVersion_IdPromocionVersionAndEstadoTrue(
            Long idPromocionVersion,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "promocionVersion",
            "promocionVersion.promocion",
            "sku",
            "sku.producto"
    })
    Page<PromocionSkuDescuentoVersion> findBySku_IdSkuAndEstadoTrue(Long idSku, Pageable pageable);

    @EntityGraph(attributePaths = {
            "promocionVersion",
            "promocionVersion.promocion",
            "sku",
            "sku.producto"
    })
    Page<PromocionSkuDescuentoVersion> findByTipoDescuentoAndEstadoTrue(
            TipoDescuento tipoDescuento,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "promocionVersion",
            "promocionVersion.promocion",
            "sku",
            "sku.producto"
    })
    List<PromocionSkuDescuentoVersion>
    findByPromocionVersion_IdPromocionVersionAndEstadoTrueOrderByPrioridadAscIdPromocionSkuDescuentoVersionAsc(
            Long idPromocionVersion
    );

    @EntityGraph(attributePaths = {
            "promocionVersion",
            "promocionVersion.promocion",
            "sku",
            "sku.producto"
    })
    List<PromocionSkuDescuentoVersion>
    findByPromocionVersion_Promocion_IdPromocionAndEstadoTrueOrderByPrioridadAscIdPromocionSkuDescuentoVersionAsc(
            Long idPromocion
    );

    @EntityGraph(attributePaths = {
            "promocionVersion",
            "promocionVersion.promocion",
            "sku",
            "sku.producto"
    })
    @Query("""
            select d
            from PromocionSkuDescuentoVersion d
            join d.promocionVersion pv
            where d.estado = true
              and pv.estado = true
              and pv.vigente = true
              and pv.visiblePublico = true
              and d.sku.idSku = :idSku
              and pv.estadoPromocion in :estados
              and pv.fechaInicio <= :fecha
              and pv.fechaFin >= :fecha
            order by d.prioridad asc, pv.fechaInicio asc, d.idPromocionSkuDescuentoVersion asc
            """)
    List<PromocionSkuDescuentoVersion> findDescuentosAplicablesBySkuAt(
            @Param("idSku") Long idSku,
            @Param("estados") Collection<EstadoPromocion> estados,
            @Param("fecha") LocalDateTime fecha
    );
}