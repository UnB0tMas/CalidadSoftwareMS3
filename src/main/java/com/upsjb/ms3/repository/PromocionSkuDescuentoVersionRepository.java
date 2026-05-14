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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromocionSkuDescuentoVersionRepository extends
        JpaRepository<PromocionSkuDescuentoVersion, Long>,
        JpaSpecificationExecutor<PromocionSkuDescuentoVersion> {

    Optional<PromocionSkuDescuentoVersion> findByIdPromocionSkuDescuentoVersionAndEstadoTrue(
            Long idPromocionSkuDescuentoVersion
    );

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

    Page<PromocionSkuDescuentoVersion> findByEstadoTrue(Pageable pageable);

    Page<PromocionSkuDescuentoVersion> findByPromocionVersion_IdPromocionVersionAndEstadoTrue(
            Long idPromocionVersion,
            Pageable pageable
    );

    Page<PromocionSkuDescuentoVersion> findBySku_IdSkuAndEstadoTrue(Long idSku, Pageable pageable);

    Page<PromocionSkuDescuentoVersion> findByTipoDescuentoAndEstadoTrue(
            TipoDescuento tipoDescuento,
            Pageable pageable
    );

    List<PromocionSkuDescuentoVersion>
    findByPromocionVersion_IdPromocionVersionAndEstadoTrueOrderByPrioridadAscIdPromocionSkuDescuentoVersionAsc(
            Long idPromocionVersion
    );

    List<PromocionSkuDescuentoVersion>
    findByPromocionVersion_Promocion_IdPromocionAndEstadoTrueOrderByPrioridadAscIdPromocionSkuDescuentoVersionAsc(
            Long idPromocion
    );

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