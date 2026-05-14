// ruta: src/main/java/com/upsjb/ms3/repository/PromocionVersionRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.PromocionVersion;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
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

public interface PromocionVersionRepository extends
        JpaRepository<PromocionVersion, Long>,
        JpaSpecificationExecutor<PromocionVersion> {

    Optional<PromocionVersion> findByIdPromocionVersionAndEstadoTrue(Long idPromocionVersion);

    Optional<PromocionVersion>
    findFirstByPromocion_IdPromocionAndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPromocionVersionDesc(
            Long idPromocion
    );

    boolean existsByPromocion_IdPromocionAndVigenteTrueAndEstadoTrue(Long idPromocion);

    boolean existsByPromocion_IdPromocionAndVigenteTrueAndEstadoTrueAndIdPromocionVersionNot(
            Long idPromocion,
            Long idPromocionVersion
    );

    Page<PromocionVersion> findByEstadoTrue(Pageable pageable);

    Page<PromocionVersion> findByPromocion_IdPromocionAndEstadoTrue(Long idPromocion, Pageable pageable);

    Page<PromocionVersion> findByEstadoPromocionAndEstadoTrue(
            EstadoPromocion estadoPromocion,
            Pageable pageable
    );

    List<PromocionVersion> findByPromocion_IdPromocionAndEstadoTrueOrderByFechaInicioDescIdPromocionVersionDesc(
            Long idPromocion
    );

    List<PromocionVersion> findByEstadoPromocionInAndVisiblePublicoTrueAndEstadoTrueOrderByFechaInicioAsc(
            Collection<EstadoPromocion> estados
    );

    @Query("""
            select pv
            from PromocionVersion pv
            where pv.estado = true
              and pv.promocion.idPromocion = :idPromocion
              and pv.fechaInicio <= :fechaFin
              and pv.fechaFin >= :fechaInicio
              and (:idPromocionVersionExcluir is null or pv.idPromocionVersion <> :idPromocionVersionExcluir)
            """)
    List<PromocionVersion> findVersionesSolapadas(
            @Param("idPromocion") Long idPromocion,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("idPromocionVersionExcluir") Long idPromocionVersionExcluir
    );

    @Query("""
            select pv
            from PromocionVersion pv
            where pv.estado = true
              and pv.vigente = true
              and pv.visiblePublico = true
              and pv.estadoPromocion in :estados
              and pv.fechaInicio <= :fecha
              and pv.fechaFin >= :fecha
            order by pv.fechaInicio asc, pv.idPromocionVersion asc
            """)
    List<PromocionVersion> findPublicasAplicablesAt(
            @Param("estados") Collection<EstadoPromocion> estados,
            @Param("fecha") LocalDateTime fecha
    );
}