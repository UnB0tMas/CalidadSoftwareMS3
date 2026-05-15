// ruta: src/main/java/com/upsjb/ms3/repository/EventoDominioOutboxRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.EventoDominioOutbox;
import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.EstadoPublicacionEvento;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoDominioOutboxRepository extends
        JpaRepository<EventoDominioOutbox, Long>,
        JpaSpecificationExecutor<EventoDominioOutbox> {

    Optional<EventoDominioOutbox> findByIdEventoAndEstadoTrue(Long idEvento);

    Optional<EventoDominioOutbox> findByEventIdAndEstadoTrue(UUID eventId);

    boolean existsByEventIdAndEstadoTrue(UUID eventId);

    boolean existsByAggregateTypeAndAggregateIdAndEventTypeAndEstadoTrue(
            AggregateType aggregateType,
            String aggregateId,
            String eventType
    );

    Page<EventoDominioOutbox> findByEstadoTrue(Pageable pageable);

    Page<EventoDominioOutbox> findByEstadoPublicacionAndEstadoTrue(
            EstadoPublicacionEvento estadoPublicacion,
            Pageable pageable
    );

    Page<EventoDominioOutbox> findByAggregateTypeAndEstadoTrue(AggregateType aggregateType, Pageable pageable);

    Page<EventoDominioOutbox> findByAggregateTypeAndAggregateIdAndEstadoTrue(
            AggregateType aggregateType,
            String aggregateId,
            Pageable pageable
    );

    Page<EventoDominioOutbox> findByEventTypeAndEstadoTrue(String eventType, Pageable pageable);

    Page<EventoDominioOutbox> findByTopicAndEstadoTrue(String topic, Pageable pageable);

    Page<EventoDominioOutbox> findByEventKeyAndEstadoTrue(String eventKey, Pageable pageable);

    List<EventoDominioOutbox> findByEstadoPublicacionAndEstadoTrueOrderByCreatedAtAscIdEventoAsc(
            EstadoPublicacionEvento estadoPublicacion
    );

    List<EventoDominioOutbox> findByAggregateTypeAndAggregateIdAndEstadoTrueOrderByCreatedAtDescIdEventoDesc(
            AggregateType aggregateType,
            String aggregateId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e
            from EventoDominioOutbox e
            where e.idEvento = :idEvento
              and e.estado = true
            """)
    Optional<EventoDominioOutbox> findActivoByIdForUpdate(@Param("idEvento") Long idEvento);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e
            from EventoDominioOutbox e
            where e.estado = true
              and e.estadoPublicacion in :estados
              and (
                    e.lockedAt is null
                    or e.lockedAt < :lockedAtBefore
                  )
            order by e.createdAt asc, e.idEvento asc
            """)
    List<EventoDominioOutbox> findPublicablesForUpdate(
            @Param("estados") Collection<EstadoPublicacionEvento> estados,
            @Param("lockedAtBefore") LocalDateTime lockedAtBefore,
            Pageable pageable
    );

    @Query("""
            select e
            from EventoDominioOutbox e
            where e.estado = true
              and e.estadoPublicacion = :estadoPublicacion
              and e.intentosPublicacion < :maxAttempts
            order by e.createdAt asc, e.idEvento asc
            """)
    List<EventoDominioOutbox> findReintentables(
            @Param("estadoPublicacion") EstadoPublicacionEvento estadoPublicacion,
            @Param("maxAttempts") Integer maxAttempts,
            Pageable pageable
    );

    long countByEstadoPublicacionAndEstadoTrue(EstadoPublicacionEvento estadoPublicacion);

    long countByTopicAndEstadoTrue(String topic);

    long countByAggregateTypeAndAggregateIdAndEstadoTrue(AggregateType aggregateType, String aggregateId);
}