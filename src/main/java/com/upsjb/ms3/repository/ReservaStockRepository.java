// ruta: src/main/java/com/upsjb/ms3/repository/ReservaStockRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.ReservaStock;
import com.upsjb.ms3.domain.enums.EstadoReservaStock;
import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaStockRepository extends
        JpaRepository<ReservaStock, Long>,
        JpaSpecificationExecutor<ReservaStock> {

    Optional<ReservaStock> findByIdReservaStockAndEstadoTrue(Long idReservaStock);

    Optional<ReservaStock> findByCodigoReservaIgnoreCaseAndEstadoTrue(String codigoReserva);

    Optional<ReservaStock> findByReferenciaTipoAndReferenciaIdExternoAndEstadoTrue(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno
    );

    Optional<ReservaStock> findByReferenciaTipoAndReferenciaIdExternoAndSku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrue(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Long idSku,
            Long idAlmacen
    );

    boolean existsByCodigoReservaIgnoreCaseAndEstadoTrue(String codigoReserva);

    boolean existsByAlmacen_IdAlmacenAndEstadoReservaInAndEstadoTrue(
            Long idAlmacen,
            Collection<EstadoReservaStock> estadosReserva
    );

    boolean existsByReferenciaTipoAndReferenciaIdExternoAndEstadoTrue(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno
    );

    boolean existsByReferenciaTipoAndReferenciaIdExternoAndSku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrue(
            TipoReferenciaStock referenciaTipo,
            String referenciaIdExterno,
            Long idSku,
            Long idAlmacen
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select r
            from ReservaStock r
            where r.idReservaStock = :idReservaStock
              and r.estado = true
            """)
    Optional<ReservaStock> findActivoByIdForUpdate(@Param("idReservaStock") Long idReservaStock);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select r
            from ReservaStock r
            where upper(r.codigoReserva) = upper(:codigoReserva)
              and r.estado = true
            """)
    Optional<ReservaStock> findActivoByCodigoForUpdate(@Param("codigoReserva") String codigoReserva);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select r
            from ReservaStock r
            where r.referenciaTipo = :referenciaTipo
              and r.referenciaIdExterno = :referenciaIdExterno
              and r.sku.idSku = :idSku
              and r.almacen.idAlmacen = :idAlmacen
              and r.estado = true
            """)
    Optional<ReservaStock> findActivoByReferenciaForUpdate(
            @Param("referenciaTipo") TipoReferenciaStock referenciaTipo,
            @Param("referenciaIdExterno") String referenciaIdExterno,
            @Param("idSku") Long idSku,
            @Param("idAlmacen") Long idAlmacen
    );

    Page<ReservaStock> findByEstadoTrue(Pageable pageable);

    Page<ReservaStock> findByEstadoReservaAndEstadoTrue(EstadoReservaStock estadoReserva, Pageable pageable);

    Page<ReservaStock> findBySku_IdSkuAndEstadoTrue(Long idSku, Pageable pageable);

    Page<ReservaStock> findByAlmacen_IdAlmacenAndEstadoTrue(Long idAlmacen, Pageable pageable);

    Page<ReservaStock> findByReferenciaTipoAndEstadoTrue(TipoReferenciaStock referenciaTipo, Pageable pageable);

    List<ReservaStock> findByEstadoReservaInAndEstadoTrueOrderByReservadoAtAsc(
            Collection<EstadoReservaStock> estados
    );

    List<ReservaStock> findByEstadoReservaAndExpiresAtBeforeAndEstadoTrueOrderByExpiresAtAsc(
            EstadoReservaStock estadoReserva,
            LocalDateTime fechaLimite
    );
}