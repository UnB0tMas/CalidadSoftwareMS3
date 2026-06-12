Revisé el flujo actual de MS2, MS3 y los dos Compose. La infraestructura Kafka ya crea `ms2.empleado.snapshot.v1` con política `compact`, ambos microservicios usan el mismo broker y MS3 ya tiene el consumidor agregado. No es necesario modificar `docker-compose.kafka.yml`, `docker-compose(2).yml` ni `.env`.

La reconciliación quedará así:

```text
MS2 termina de arrancar
        ↓
Lee todos los empleados, activos e inactivos
        ↓
Registra sus snapshots en el Outbox
        ↓
Publica inmediatamente los eventos pendientes
        ↓
MS3 consume ms2.empleado.snapshot.v1
        ↓
Crea o actualiza empleado_snapshot_ms2
        ↓
MS3 verifica tópico, listener, eventos recibidos y snapshots
        ↓
Imprime resultado en consola
```

También corregimos un riesgo importante: el probe funcional de MS2 publica empleados sintéticos con código `KFP-EMP-*` sobre el tópico real. Sin filtrarlos, MS3 podría persistirlos como empleados reales. El consumidor actualizado los reconoce y los ignora.

## Archivos de MS2

### 1. `src/main/java/com/upsjb/ms2/repository/EmpleadoRepository.java`

```java
package com.upsjb.ms2.repository;

import com.upsjb.ms2.domain.entity.Empleado;
import jakarta.persistence.LockModeType;
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
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadoRepository extends
        JpaRepository<Empleado, Long>,
        JpaSpecificationExecutor<Empleado> {

    @Override
    @EntityGraph(attributePaths = {
            "persona",
            "area"
    })
    Optional<Empleado> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {
            "persona",
            "area"
    })
    Page<Empleado> findAll(
            Specification<Empleado> spec,
            Pageable pageable
    );

    @Override
    @EntityGraph(attributePaths = {
            "persona",
            "area"
    })
    List<Empleado> findAll(
            Specification<Empleado> spec
    );

    /*
     * Utilizado por la reconciliación de snapshots al iniciar MS2.
     *
     * Incluye empleados activos e inactivos porque MS3 también debe
     * conocer las inactivaciones y dejar de mostrarlos en sus lookups.
     */
    @EntityGraph(attributePaths = {
            "persona",
            "area"
    })
    Page<Empleado> findAllByOrderByIdAsc(
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "persona",
            "area"
    })
    @Query("""
            select e
            from Empleado e
            where e.id = :idEmpleado
            """)
    Optional<Empleado> findByIdForUpdate(
            @Param("idEmpleado") Long idEmpleado
    );

    @EntityGraph(attributePaths = {
            "persona",
            "area"
    })
    Optional<Empleado> findByIdUsuarioMs1(
            Long idUsuarioMs1
    );

    @EntityGraph(attributePaths = {
            "persona",
            "area"
    })
    Optional<Empleado> findByIdUsuarioMs1AndEstadoTrue(
            Long idUsuarioMs1
    );

    @EntityGraph(attributePaths = {
            "persona",
            "area"
    })
    Optional<Empleado> findByCodigoEmpleadoIgnoreCase(
            String codigoEmpleado
    );

    boolean existsByIdUsuarioMs1(
            Long idUsuarioMs1
    );

    boolean existsByIdUsuarioMs1AndIdNot(
            Long idUsuarioMs1,
            Long id
    );

    boolean existsByCodigoEmpleadoIgnoreCase(
            String codigoEmpleado
    );

    boolean existsByCodigoEmpleadoIgnoreCaseAndIdNot(
            String codigoEmpleado,
            Long id
    );

    boolean existsByPersona_Id(
            Long idPersona
    );

    boolean existsByPersona_IdAndEstadoTrue(
            Long idPersona
    );

    boolean existsByPersona_IdAndIdNot(
            Long idPersona,
            Long id
    );

    @EntityGraph(attributePaths = {
            "persona",
            "area"
    })
    List<Empleado> findByArea_IdAndEstadoTrueOrderByCodigoEmpleadoAsc(
            Long idArea
    );

    @EntityGraph(attributePaths = {
            "persona",
            "area"
    })
    @Query("""
            select e
            from Empleado e
            join e.persona p
            join e.area a
            where e.estado = true
              and (
                    :search is null
                    or lower(e.codigoEmpleado) like lower(concat('%', :search, '%'))
                    or lower(p.nombres) like lower(concat('%', :search, '%'))
                    or lower(p.apePaterno) like lower(concat('%', :search, '%'))
                    or lower(coalesce(p.apeMaterno, '')) like lower(concat('%', :search, '%'))
                    or lower(coalesce(p.correo.value, '')) like lower(concat('%', :search, '%'))
                    or lower(a.nombre) like lower(concat('%', :search, '%'))
              )
            order by e.codigoEmpleado asc
            """)
    List<Empleado> searchActivos(
            @Param("search") String search,
            Pageable pageable
    );
}
```

---

### 2. `src/main/java/com/upsjb/ms2/repository/EventoDominioOutboxRepository.java`

```java
package com.upsjb.ms2.repository;

import com.upsjb.ms2.domain.entity.EventoDominioOutbox;
import com.upsjb.ms2.domain.enums.AggregateType;
import com.upsjb.ms2.domain.enums.EstadoPublicacionEvento;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoDominioOutboxRepository extends
        JpaRepository<EventoDominioOutbox, Long>,
        JpaSpecificationExecutor<EventoDominioOutbox> {

    Optional<EventoDominioOutbox> findByEventId(
            UUID eventId
    );

    boolean existsByEventId(
            UUID eventId
    );

    List<EventoDominioOutbox> findByEventIdIn(
            Collection<UUID> eventIds
    );

    List<EventoDominioOutbox>
    findByAggregateTypeAndAggregateIdOrderByCreatedAtDesc(
            AggregateType aggregateType,
            Long aggregateId
    );

    List<EventoDominioOutbox>
    findByEstadoPublicacionOrderByCreatedAtAsc(
            EstadoPublicacionEvento estadoPublicacion
    );

    List<EventoDominioOutbox>
    findTop50ByEstadoPublicacionAndEstadoTrueOrderByCreatedAtAsc(
            EstadoPublicacionEvento estadoPublicacion
    );

    long countByEstadoPublicacion(
            EstadoPublicacionEvento estadoPublicacion
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e
            from EventoDominioOutbox e
            where e.id = :idEvento
            """)
    Optional<EventoDominioOutbox> findByIdForUpdate(
            @Param("idEvento") Long idEvento
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e
            from EventoDominioOutbox e
            where e.estado = true
              and e.estadoPublicacion = :estadoPublicacion
            order by e.createdAt asc
            """)
    List<EventoDominioOutbox> findForUpdateByEstadoPublicacion(
            @Param("estadoPublicacion")
            EstadoPublicacionEvento estadoPublicacion,
            Pageable pageable
    );
}
```

---

### 3. Añadir `src/main/java/com/upsjb/ms2/config/EmpleadoSnapshotStartupReconciliationProperties.java`

```java
package com.upsjb.ms2.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(
        prefix = "app.employee-snapshot-reconciliation"
)
public class EmpleadoSnapshotStartupReconciliationProperties {

    /*
     * Ejecuta una reconciliación una sola vez por cada arranque
     * de la instancia de MS2.
     */
    private boolean enabled = true;

    /*
     * Cantidad de empleados que se leen de MS2 por página.
     */
    @Min(1)
    @Max(100)
    private int pageSize = 100;

    /*
     * Cantidad máxima de eventos Outbox que se publican
     * en cada ciclo.
     */
    @Min(1)
    @Max(100)
    private int publishBatchSize = 100;

    /*
     * Límite de ciclos de publicación inmediata.
     * El scheduler normal del Outbox seguirá reintentando después.
     */
    @Min(1)
    @Max(1000)
    private int maxPublishCycles = 20;

    /*
     * Espera entre ciclos cuando hay eventos pendientes,
     * pero otro proceso o el scheduler está operándolos.
     */
    @NotNull
    private Duration retryDelay = Duration.ofMillis(500);

    /*
     * false:
     * MS2 continúa disponible aunque la reconciliación falle.
     *
     * true:
     * El error se propaga durante el arranque.
     */
    private boolean failFast = false;
}
```

---

### 4. Añadir `src/main/java/com/upsjb/ms2/kafka/reconciliation/EmpleadoSnapshotReconciliationOutboxWriter.java`

```java
package com.upsjb.ms2.kafka.reconciliation;

import com.upsjb.ms2.domain.entity.Empleado;
import com.upsjb.ms2.domain.entity.EventoDominioOutbox;
import com.upsjb.ms2.domain.enums.AggregateType;
import com.upsjb.ms2.domain.enums.EmpleadoEventType;
import com.upsjb.ms2.dto.empleado.response.EmpleadoSnapshotResponseDto;
import com.upsjb.ms2.kafka.outbox.OutboxEventFactory;
import com.upsjb.ms2.mapper.EmpleadoSnapshotMapper;
import com.upsjb.ms2.repository.EmpleadoRepository;
import com.upsjb.ms2.repository.EventoDominioOutboxRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmpleadoSnapshotReconciliationOutboxWriter {

    private final EmpleadoRepository empleadoRepository;
    private final EventoDominioOutboxRepository outboxRepository;
    private final EmpleadoSnapshotMapper empleadoSnapshotMapper;
    private final OutboxEventFactory outboxEventFactory;

    @Transactional
    public PageRegistration registerPage(
            int pageNumber,
            int pageSize
    ) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException(
                    "El número de página no puede ser negativo."
            );
        }

        if (pageSize <= 0 || pageSize > 100) {
            throw new IllegalArgumentException(
                    "El tamaño de página debe estar entre 1 y 100."
            );
        }

        Page<Empleado> employeePage =
                empleadoRepository.findAllByOrderByIdAsc(
                        PageRequest.of(
                                pageNumber,
                                pageSize
                        )
                );

        if (employeePage.isEmpty()) {
            return new PageRegistration(
                    pageNumber,
                    employeePage.getTotalPages(),
                    employeePage.getTotalElements(),
                    false,
                    List.of()
            );
        }

        List<EventoDominioOutbox> events =
                new ArrayList<>(
                        employeePage.getNumberOfElements()
                );

        for (Empleado empleado : employeePage.getContent()) {
            if (
                    empleado == null
                            || empleado.getId() == null
            ) {
                continue;
            }

            UUID eventId =
                    UUID.randomUUID();

            EmpleadoEventType eventType =
                    empleado.isActivo()
                            ? EmpleadoEventType
                            .EmpleadoSnapshotActualizado
                            : EmpleadoEventType
                            .EmpleadoSnapshotInactivado;

            EmpleadoSnapshotResponseDto snapshot =
                    empleadoSnapshotMapper.toSnapshot(
                            eventId,
                            eventType.name(),
                            empleado
                    );

            EventoDominioOutbox event =
                    outboxEventFactory.create(
                            eventId,
                            AggregateType.EMPLEADO,
                            empleado.getId(),
                            eventType,
                            snapshot
                    );

            events.add(
                    event
            );
        }

        List<EventoDominioOutbox> savedEvents =
                events.isEmpty()
                        ? List.of()
                        : outboxRepository.saveAll(
                                events
                        );

        if (!savedEvents.isEmpty()) {
            outboxRepository.flush();
        }

        List<UUID> eventIds =
                savedEvents.stream()
                        .map(
                                EventoDominioOutbox::getEventId
                        )
                        .toList();

        log.info(
                "Página de reconciliación de empleados registrada en Outbox. "
                        + "page={}, pageSize={}, employeeCount={}, eventCount={}, "
                        + "totalElements={}, totalPages={}, hasNext={}",
                pageNumber,
                pageSize,
                employeePage.getNumberOfElements(),
                eventIds.size(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages(),
                employeePage.hasNext()
        );

        return new PageRegistration(
                pageNumber,
                employeePage.getTotalPages(),
                employeePage.getTotalElements(),
                employeePage.hasNext(),
                eventIds
        );
    }

    public record PageRegistration(
            int page,
            int totalPages,
            long totalEmployees,
            boolean hasNext,
            List<UUID> eventIds
    ) {

        public PageRegistration {
            eventIds =
                    eventIds == null
                            ? List.of()
                            : List.copyOf(
                                    eventIds
                            );
        }
    }
}
```

---

### 5. Añadir `src/main/java/com/upsjb/ms2/kafka/reconciliation/EmpleadoSnapshotStartupReconciliationRunner.java`

```java
package com.upsjb.ms2.kafka.reconciliation;

import com.upsjb.ms2.config.EmpleadoSnapshotStartupReconciliationProperties;
import com.upsjb.ms2.domain.entity.EventoDominioOutbox;
import com.upsjb.ms2.domain.enums.EstadoPublicacionEvento;
import com.upsjb.ms2.dto.outbox.response.EventoDominioOutboxResponseDto;
import com.upsjb.ms2.kafka.reconciliation.EmpleadoSnapshotReconciliationOutboxWriter.PageRegistration;
import com.upsjb.ms2.repository.EventoDominioOutboxRepository;
import com.upsjb.ms2.service.contract.KafkaPublisherService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmpleadoSnapshotStartupReconciliationRunner {

    private static final int STATUS_QUERY_BATCH_SIZE =
            500;

    private final EmpleadoSnapshotStartupReconciliationProperties
            properties;

    private final EmpleadoSnapshotReconciliationOutboxWriter
            outboxWriter;

    private final EventoDominioOutboxRepository
            outboxRepository;

    private final KafkaPublisherService
            kafkaPublisherService;

    private final AtomicBoolean executed =
            new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void reconcileOnStartup() {
        if (!properties.isEnabled()) {
            log.info(
                    "Reconciliación automática de empleados MS2 -> Kafka "
                            + "deshabilitada."
            );

            return;
        }

        if (!executed.compareAndSet(false, true)) {
            log.debug(
                    "La reconciliación automática de empleados ya fue "
                            + "ejecutada en esta instancia."
            );

            return;
        }

        long startedAt =
                System.nanoTime();

        try {
            log.info(
                    "============================================================"
            );
            log.info(
                    "INICIANDO RECONCILIACIÓN AUTOMÁTICA DE EMPLEADOS MS2 -> MS3"
            );
            log.info(
                    "pageSize={}, publishBatchSize={}, maxPublishCycles={}",
                    properties.getPageSize(),
                    properties.getPublishBatchSize(),
                    properties.getMaxPublishCycles()
            );
            log.info(
                    "============================================================"
            );

            List<UUID> reconciliationEventIds =
                    registerAllEmployees();

            if (reconciliationEventIds.isEmpty()) {
                log.info(
                        "Reconciliación MS2 -> MS3 finalizada sin eventos: "
                                + "MS2 no contiene empleados para sincronizar."
                );

                return;
            }

            int publicationCycles =
                    publishUntilTerminal(
                            reconciliationEventIds
                    );

            PublicationSummary summary =
                    summarize(
                            reconciliationEventIds
                    );

            long elapsedMillis =
                    elapsedMillis(
                            startedAt
                    );

            if (summary.successful()) {
                log.info(
                        "============================================================"
                );
                log.info(
                        "RECONCILIACIÓN AUTOMÁTICA MS2 -> MS3 COMPLETADA"
                );
                log.info(
                        "eventosRegistrados={}, publicados={}, pendientes={}, "
                                + "errores={}, faltantes={}, ciclos={}, duracionMs={}",
                        summary.total(),
                        summary.published(),
                        summary.pending(),
                        summary.errors(),
                        summary.missing(),
                        publicationCycles,
                        elapsedMillis
                );
                log.info(
                        "Los eventos fueron enviados a Kafka. MS3 los recibirá "
                                + "mediante su consumidor de empleados."
                );
                log.info(
                        "============================================================"
                );

                return;
            }

            String message =
                    "La reconciliación automática MS2 -> MS3 terminó "
                            + "con incidencias. eventosRegistrados="
                            + summary.total()
                            + ", publicados="
                            + summary.published()
                            + ", pendientes="
                            + summary.pending()
                            + ", errores="
                            + summary.errors()
                            + ", faltantes="
                            + summary.missing()
                            + ", ciclos="
                            + publicationCycles
                            + ", duracionMs="
                            + elapsedMillis;

            log.error(
                    "============================================================"
            );
            log.error(
                    "RECONCILIACIÓN AUTOMÁTICA MS2 -> MS3 CON INCIDENCIAS"
            );
            log.error(
                    message
            );
            log.error(
                    "El scheduler Outbox continuará intentando publicar "
                            + "los eventos pendientes."
            );
            log.error(
                    "============================================================"
            );

            if (properties.isFailFast()) {
                throw new IllegalStateException(
                        message
                );
            }
        } catch (RuntimeException ex) {
            log.error(
                    "Error ejecutando la reconciliación automática de "
                            + "empleados MS2 -> MS3. detalle={}",
                    safeMessage(ex),
                    ex
            );

            if (properties.isFailFast()) {
                throw ex;
            }
        }
    }

    private List<UUID> registerAllEmployees() {
        List<UUID> eventIds =
                new ArrayList<>();

        int pageNumber =
                0;

        boolean hasNext;

        do {
            PageRegistration registration =
                    outboxWriter.registerPage(
                            pageNumber,
                            properties.getPageSize()
                    );

            eventIds.addAll(
                    registration.eventIds()
            );

            hasNext =
                    registration.hasNext();

            pageNumber++;
        } while (hasNext);

        log.info(
                "Registro Outbox de reconciliación completado. "
                        + "eventosRegistrados={}",
                eventIds.size()
        );

        return List.copyOf(
                eventIds
        );
    }

    private int publishUntilTerminal(
            List<UUID> eventIds
    ) {
        int completedCycles =
                0;

        for (
                int cycle = 1;
                cycle <= properties.getMaxPublishCycles();
                cycle++
        ) {
            PublicationSummary before =
                    summarize(
                            eventIds
                    );

            if (before.pending() == 0) {
                return completedCycles;
            }

            List<EventoDominioOutboxResponseDto> processed =
                    kafkaPublisherService.publicarPendientes(
                            properties.getPublishBatchSize()
                    );

            completedCycles =
                    cycle;

            PublicationSummary after =
                    summarize(
                            eventIds
                    );

            log.info(
                    "Ciclo de publicación de reconciliación procesado. "
                            + "cycle={}, outboxProcessed={}, published={}, "
                            + "pending={}, errors={}, missing={}",
                    cycle,
                    processed == null
                            ? 0
                            : processed.size(),
                    after.published(),
                    after.pending(),
                    after.errors(),
                    after.missing()
            );

            if (after.pending() == 0) {
                return completedCycles;
            }

            if (
                    processed == null
                            || processed.isEmpty()
            ) {
                sleepSafely(
                        properties.getRetryDelay()
                );
            }
        }

        return completedCycles;
    }

    private PublicationSummary summarize(
            List<UUID> eventIds
    ) {
        if (
                eventIds == null
                        || eventIds.isEmpty()
        ) {
            return PublicationSummary.empty();
        }

        Set<UUID> uniqueIds =
                new HashSet<>(
                        eventIds
                );

        long published =
                0;

        long pending =
                0;

        long errors =
                0;

        long found =
                0;

        List<UUID> allIds =
                new ArrayList<>(
                        uniqueIds
                );

        for (
                int from = 0;
                from < allIds.size();
                from += STATUS_QUERY_BATCH_SIZE
        ) {
            int to =
                    Math.min(
                            from + STATUS_QUERY_BATCH_SIZE,
                            allIds.size()
                    );

            List<UUID> batch =
                    allIds.subList(
                            from,
                            to
                    );

            List<EventoDominioOutbox> events =
                    outboxRepository.findByEventIdIn(
                            batch
                    );

            for (EventoDominioOutbox event : events) {
                if (event == null) {
                    continue;
                }

                found++;

                EstadoPublicacionEvento status =
                        event.getEstadoPublicacion();

                if (
                        EstadoPublicacionEvento.PUBLICADO
                                .equals(status)
                ) {
                    published++;
                } else if (
                        EstadoPublicacionEvento.ERROR
                                .equals(status)
                ) {
                    errors++;
                } else {
                    pending++;
                }
            }
        }

        long missing =
                Math.max(
                        0,
                        uniqueIds.size() - found
                );

        return new PublicationSummary(
                uniqueIds.size(),
                published,
                pending,
                errors,
                missing
        );
    }

    private void sleepSafely(
            Duration duration
    ) {
        if (
                duration == null
                        || duration.isNegative()
                        || duration.isZero()
        ) {
            return;
        }

        try {
            Thread.sleep(
                    duration.toMillis()
            );
        } catch (InterruptedException ex) {
            Thread.currentThread()
                    .interrupt();

            throw new IllegalStateException(
                    "La reconciliación de empleados fue interrumpida.",
                    ex
            );
        }
    }

    private long elapsedMillis(
            long startedAt
    ) {
        return Duration.ofNanos(
                        System.nanoTime() - startedAt
                )
                .toMillis();
    }

    private String safeMessage(
            Throwable throwable
    ) {
        if (
                throwable == null
                        || throwable.getMessage() == null
                        || throwable.getMessage().isBlank()
        ) {
            return "error no especificado";
        }

        String message =
                throwable.getMessage()
                        .trim();

        return message.length() <= 500
                ? message
                : message.substring(
                        0,
                        500
                );
    }

    private record PublicationSummary(
            long total,
            long published,
            long pending,
            long errors,
            long missing
    ) {

        private static PublicationSummary empty() {
            return new PublicationSummary(
                    0,
                    0,
                    0,
                    0,
                    0
            );
        }

        private boolean successful() {
            return pending == 0
                    && errors == 0
                    && missing == 0;
        }
    }
}
```

## Archivos de MS3

### 6. `src/main/java/com/upsjb/ms3/kafka/event/Ms2EmpleadoSnapshotEvent.java`

```java
package com.upsjb.ms3.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Ms2EmpleadoSnapshotEvent(
        UUID eventId,
        String eventType,
        String aggregateType,
        Long aggregateId,
        String producer,
        Instant occurredAt,
        Integer version,
        Ms2EmpleadoSnapshotPayload data,
        JsonNode actor,
        JsonNode trace
) {

    private static final String EXPECTED_AGGREGATE_TYPE =
            "EMPLEADO";

    private static final String FUNCTIONAL_PROBE_CODE_PREFIX =
            "KFP-EMP-";

    private static final String FUNCTIONAL_PROBE_PATH_TOKEN =
            "kafka-functional-probe";

    private static final String FUNCTIONAL_PROBE_USER_AGENT =
            "kafka-functional-probe";

    public void validate() {
        if (eventId == null) {
            throw new IllegalArgumentException(
                    "El evento de empleado MS2 no contiene eventId."
            );
        }

        requireText(
                eventType,
                "El evento de empleado MS2 no contiene eventType."
        );

        requireText(
                aggregateType,
                "El evento de empleado MS2 no contiene aggregateType."
        );

        if (
                !EXPECTED_AGGREGATE_TYPE.equalsIgnoreCase(
                        aggregateType.trim()
                )
        ) {
            throw new IllegalArgumentException(
                    "El aggregateType del evento MS2 debe ser EMPLEADO."
            );
        }

        requirePositive(
                aggregateId,
                "El aggregateId del evento de empleado MS2 "
                        + "debe ser mayor a cero."
        );

        if (occurredAt == null) {
            throw new IllegalArgumentException(
                    "El evento de empleado MS2 no contiene occurredAt."
            );
        }

        if (data == null) {
            throw new IllegalArgumentException(
                    "El evento de empleado MS2 no contiene el payload data."
            );
        }

        data.validate();

        if (
                !Objects.equals(
                        aggregateId,
                        data.idEmpleado()
                )
        ) {
            throw new IllegalArgumentException(
                    "El aggregateId del evento no coincide con "
                            + "el idEmpleado del payload."
            );
        }

        requireText(
                nombresCompletos(),
                "El empleado MS2 debe contener nombres completos."
        );
    }

    /*
     * El probe E2E de MS2 publica un empleado sintético sobre el
     * tópico funcional real. MS3 debe validarlo, pero no persistirlo.
     */
    public boolean isFunctionalProbe() {
        String codigo =
                data == null
                        ? null
                        : clean(
                                data.codigoEmpleado()
                        );

        if (
                codigo != null
                        && codigo.toUpperCase(
                                Locale.ROOT
                        )
                        .startsWith(
                                FUNCTIONAL_PROBE_CODE_PREFIX
                        )
        ) {
            return true;
        }

        String path =
                traceText(
                        "path"
                );

        if (
                path != null
                        && path.toLowerCase(
                                Locale.ROOT
                        )
                        .contains(
                                FUNCTIONAL_PROBE_PATH_TOKEN
                        )
        ) {
            return true;
        }

        String userAgent =
                traceText(
                        "userAgent"
                );

        return userAgent != null
                && FUNCTIONAL_PROBE_USER_AGENT.equalsIgnoreCase(
                        userAgent
                );
    }

    public LocalDateTime snapshotAt() {
        if (occurredAt == null) {
            throw new IllegalStateException(
                    "No se puede calcular snapshotAt porque occurredAt es nulo."
            );
        }

        return LocalDateTime.ofInstant(
                occurredAt,
                ZoneOffset.UTC
        );
    }

    public Long snapshotVersion() {
        if (occurredAt == null) {
            throw new IllegalStateException(
                    "No se puede calcular snapshotVersion porque "
                            + "occurredAt es nulo."
            );
        }

        return occurredAt.toEpochMilli();
    }

    public String nombresCompletos() {
        if (
                data == null
                        || data.persona() == null
        ) {
            return null;
        }

        return data.persona()
                .nombresCompletos();
    }

    private String traceText(
            String field
    ) {
        if (
                trace == null
                        || trace.isNull()
                        || field == null
                        || field.isBlank()
        ) {
            return null;
        }

        JsonNode value =
                trace.get(
                        field
                );

        if (
                value == null
                        || value.isNull()
                        || !value.isValueNode()
        ) {
            return null;
        }

        return clean(
                value.asText()
        );
    }

    private static void requirePositive(
            Long value,
            String message
    ) {
        if (
                value == null
                        || value <= 0
        ) {
            throw new IllegalArgumentException(
                    message
            );
        }
    }

    private static void requireText(
            String value,
            String message
    ) {
        if (clean(value) == null) {
            throw new IllegalArgumentException(
                    message
            );
        }
    }

    private static String clean(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );

        return normalized.isBlank()
                ? null
                : normalized;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ms2EmpleadoSnapshotPayload(
            Long idEmpleado,
            Long idUsuarioMs1,
            String codigoEmpleado,
            Boolean estado,
            Long idArea,
            String areaCodigo,
            String areaNombre,
            Ms2PersonaSnapshotPayload persona,
            LocalDate fechaIngreso,
            LocalDate fechaCese,
            Boolean puedeCrear,
            Boolean puedeActualizar
    ) {

        public void validate() {
            requirePositive(
                    idEmpleado,
                    "El idEmpleado del snapshot MS2 debe ser mayor a cero."
            );

            requirePositive(
                    idUsuarioMs1,
                    "El idUsuarioMs1 del snapshot MS2 debe ser mayor a cero."
            );

            requireText(
                    codigoEmpleado,
                    "El código del empleado MS2 es obligatorio."
            );

            if (estado == null) {
                throw new IllegalArgumentException(
                        "El estado del empleado MS2 es obligatorio."
                );
            }

            if (persona == null) {
                throw new IllegalArgumentException(
                        "Los datos personales del empleado MS2 son obligatorios."
                );
            }

            requireText(
                    persona.nombresCompletos(),
                    "Los nombres completos del empleado MS2 son obligatorios."
            );
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ms2PersonaSnapshotPayload(
            Long idPersona,
            String nombres,
            String apePaterno,
            String apeMaterno,
            String tipoDoc,
            String numeroDoc,
            String correo,
            JsonNode ubigeo
    ) {

        public String nombresCompletos() {
            String result =
                    Stream.of(
                                    nombres,
                                    apePaterno,
                                    apeMaterno
                            )
                            .map(
                                    Ms2EmpleadoSnapshotEvent::clean
                            )
                            .filter(
                                    Objects::nonNull
                            )
                            .collect(
                                    Collectors.joining(
                                            " "
                                    )
                            );

            return result.isBlank()
                    ? null
                    : result;
        }
    }
}
```

---

### 7. Añadir `src/main/java/com/upsjb/ms3/kafka/consumer/Ms2EmpleadoSnapshotConsumerMetrics.java`

```java
package com.upsjb.ms3.kafka.consumer;

import com.upsjb.ms3.kafka.consumer.Ms2EmpleadoSnapshotHandler.Ms2EmpleadoSnapshotResult;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class Ms2EmpleadoSnapshotConsumerMetrics {

    private final AtomicLong received =
            new AtomicLong();

    private final AtomicLong handled =
            new AtomicLong();

    private final AtomicLong processed =
            new AtomicLong();

    private final AtomicLong created =
            new AtomicLong();

    private final AtomicLong updated =
            new AtomicLong();

    private final AtomicLong duplicated =
            new AtomicLong();

    private final AtomicLong stale =
            new AtomicLong();

    private final AtomicLong functionalProbesIgnored =
            new AtomicLong();

    private final AtomicLong failures =
            new AtomicLong();

    public void recordReceived() {
        received.incrementAndGet();
    }

    public void recordResult(
            Ms2EmpleadoSnapshotResult result
    ) {
        if (result == null) {
            return;
        }

        handled.incrementAndGet();

        if (result.processed()) {
            processed.incrementAndGet();

            if (result.created()) {
                created.incrementAndGet();
            } else {
                updated.incrementAndGet();
            }
        }

        if (result.duplicated()) {
            duplicated.incrementAndGet();
        }

        if (result.stale()) {
            stale.incrementAndGet();
        }
    }

    public void recordFunctionalProbeIgnored() {
        functionalProbesIgnored.incrementAndGet();
    }

    public void recordFailure() {
        failures.incrementAndGet();
    }

    public Snapshot snapshot() {
        return new Snapshot(
                received.get(),
                handled.get(),
                processed.get(),
                created.get(),
                updated.get(),
                duplicated.get(),
                stale.get(),
                functionalProbesIgnored.get(),
                failures.get()
        );
    }

    public record Snapshot(
            long received,
            long handled,
            long processed,
            long created,
            long updated,
            long duplicated,
            long stale,
            long functionalProbesIgnored,
            long failures
    ) {
    }
}
```

---

### 8. `src/main/java/com/upsjb/ms3/kafka/consumer/Ms2EmpleadoSnapshotConsumer.java`

```java
package com.upsjb.ms3.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms3.kafka.consumer.Ms2EmpleadoSnapshotHandler.Ms2EmpleadoSnapshotResult;
import com.upsjb.ms3.kafka.event.Ms2EmpleadoSnapshotEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class Ms2EmpleadoSnapshotConsumer {

    public static final String LISTENER_ID =
            "ms2EmpleadoSnapshotConsumer";

    private final ObjectMapper objectMapper;
    private final Ms2EmpleadoSnapshotHandler handler;
    private final Ms2EmpleadoSnapshotConsumerMetrics metrics;

    @KafkaListener(
            id = LISTENER_ID,
            topics = "${app.kafka.topics.ms2-empleado-snapshot:"
                    + "ms2.empleado.snapshot.v1}",
            groupId = "${app.kafka.consumer-groups.ms2-empleado-snapshot:"
                    + "ms3-empleado-snapshot-consumer}",
            clientIdPrefix = "ms3-empleado-snapshot",
            containerFactory = "kafkaListenerContainerFactory",
            autoStartup = "${app.kafka.enabled:true}"
    )
    public void consume(
            ConsumerRecord<String, String> record
    ) throws JsonProcessingException {
        metrics.recordReceived();

        try {
            validateRecord(
                    record
            );

            Ms2EmpleadoSnapshotEvent event =
                    deserialize(
                            record
                    );

            /*
             * Se valida antes de decidir si es un probe.
             * Un mensaje sintético también debe cumplir el contrato.
             */
            event.validate();

            if (event.isFunctionalProbe()) {
                metrics.recordFunctionalProbeIgnored();

                log.info(
                        "Probe funcional de empleado MS2 validado e ignorado. "
                                + "No se persistirá como empleado real. "
                                + "topic={}, partition={}, offset={}, key={}, "
                                + "eventId={}, idEmpleadoMs2={}, codigoEmpleado={}",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.key(),
                        event.eventId(),
                        event.data().idEmpleado(),
                        event.data().codigoEmpleado()
                );

                return;
            }

            Ms2EmpleadoSnapshotResult result =
                    handler.handle(
                            record,
                            event
                    );

            metrics.recordResult(
                    result
            );

            log.info(
                    "Evento de empleado MS2 atendido. "
                            + "topic={}, partition={}, offset={}, key={}, "
                            + "eventId={}, idEmpleadoSnapshot={}, idEmpleadoMs2={}, "
                            + "processed={}, created={}, duplicated={}, stale={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    result.eventId(),
                    result.idEmpleadoSnapshot(),
                    result.idEmpleadoMs2(),
                    result.processed(),
                    result.created(),
                    result.duplicated(),
                    result.stale()
            );
        } catch (
                JsonProcessingException
                | RuntimeException ex
        ) {
            metrics.recordFailure();

            log.error(
                    "Error procesando evento de empleado MS2. "
                            + "topic={}, partition={}, offset={}, key={}, detail={}",
                    record == null
                            ? null
                            : record.topic(),
                    record == null
                            ? null
                            : record.partition(),
                    record == null
                            ? null
                            : record.offset(),
                    record == null
                            ? null
                            : record.key(),
                    safeMessage(ex),
                    ex
            );

            throw ex;
        }
    }

    private Ms2EmpleadoSnapshotEvent deserialize(
            ConsumerRecord<String, String> record
    ) throws JsonProcessingException {
        try {
            return objectMapper.readValue(
                    record.value(),
                    Ms2EmpleadoSnapshotEvent.class
            );
        } catch (JsonProcessingException ex) {
            log.error(
                    "No se pudo deserializar el evento de empleado MS2. "
                            + "topic={}, partition={}, offset={}, key={}, error={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    ex.getMessage()
            );

            throw ex;
        }
    }

    private void validateRecord(
            ConsumerRecord<String, String> record
    ) {
        if (record == null) {
            throw new IllegalArgumentException(
                    "El registro Kafka de empleado MS2 es obligatorio."
            );
        }

        if (!StringUtils.hasText(record.value())) {
            throw new IllegalArgumentException(
                    "El mensaje Kafka de empleado MS2 está vacío."
            );
        }
    }

    private String safeMessage(
            Throwable throwable
    ) {
        if (
                throwable == null
                        || throwable.getMessage() == null
                        || throwable.getMessage().isBlank()
        ) {
            return "error no especificado";
        }

        String message =
                throwable.getMessage()
                        .trim();

        return message.length() <= 500
                ? message
                : message.substring(
                        0,
                        500
                );
    }
}
```

---

### 9. Añadir `src/main/java/com/upsjb/ms3/config/EmpleadoSnapshotStartupVerificationProperties.java`

```java
package com.upsjb.ms3.config;

import com.upsjb.ms3.kafka.consumer.Ms2EmpleadoSnapshotConsumer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(
        prefix = "app.employee-snapshot-startup-verification"
)
public class EmpleadoSnapshotStartupVerificationProperties {

    private boolean enabled = true;

    @NotBlank
    private String topic =
            "ms2.empleado.snapshot.v1";

    @NotBlank
    private String listenerId =
            Ms2EmpleadoSnapshotConsumer.LISTENER_ID;

    @NotNull
    private Duration initialDelay =
            Duration.ofSeconds(2);

    @NotNull
    private Duration retryDelay =
            Duration.ofSeconds(2);

    @NotNull
    private Duration adminTimeout =
            Duration.ofSeconds(5);

    @Min(1)
    @Max(100)
    private int maxAttempts =
            30;

    /*
     * Solo cuenta eventos reales de empleados.
     * Los eventos KFP-EMP-* del probe no se consideran.
     */
    @Min(0)
    private long minimumConsumedEvents =
            1;

    @Min(0)
    private long minimumSnapshots =
            1;

    /*
     * En local permanece false para no derribar MS3 por una demora
     * temporal de MS2. En un despliegue estricto puede activarse.
     */
    private boolean failFast =
            false;
}
```

---

### 10. Añadir `src/main/java/com/upsjb/ms3/kafka/reconciliation/Ms2EmpleadoSnapshotStartupVerifier.java`

```java
package com.upsjb.ms3.kafka.reconciliation;

import com.upsjb.ms3.config.EmpleadoSnapshotStartupVerificationProperties;
import com.upsjb.ms3.kafka.consumer.Ms2EmpleadoSnapshotConsumerMetrics;
import com.upsjb.ms3.kafka.consumer.Ms2EmpleadoSnapshotConsumerMetrics.Snapshot;
import com.upsjb.ms3.repository.EmpleadoSnapshotMs2Repository;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Ms2EmpleadoSnapshotStartupVerifier {

    private final EmpleadoSnapshotStartupVerificationProperties
            properties;

    private final KafkaAdmin kafkaAdmin;

    private final KafkaListenerEndpointRegistry
            listenerRegistry;

    private final Ms2EmpleadoSnapshotConsumerMetrics
            consumerMetrics;

    private final EmpleadoSnapshotMs2Repository
            empleadoSnapshotRepository;

    private final AtomicBoolean executed =
            new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void verifyOnStartup() {
        if (!properties.isEnabled()) {
            log.info(
                    "Verificación de reconciliación MS2 -> MS3 "
                            + "deshabilitada."
            );

            return;
        }

        if (!executed.compareAndSet(false, true)) {
            return;
        }

        sleepSafely(
                properties.getInitialDelay()
        );

        VerificationResult lastResult =
                null;

        for (
                int attempt = 1;
                attempt <= properties.getMaxAttempts();
                attempt++
        ) {
            lastResult =
                    verifyCurrentState(
                            attempt
                    );

            if (lastResult.successful()) {
                log.info(
                        "============================================================"
                );
                log.info(
                        "RECONCILIACIÓN DE EMPLEADOS MS2 -> MS3 VERIFICADA"
                );
                log.info(
                        "topic={}, partitions={}, cleanupPolicy={}, "
                                + "listenerRunning={}, eventosRecibidos={}, "
                                + "eventosProcesados={}, creados={}, actualizados={}, "
                                + "duplicados={}, antiguos={}, probesIgnorados={}, "
                                + "erroresConsumer={}, snapshotsLocales={}, attempt={}",
                        properties.getTopic(),
                        lastResult.topicStatus().partitions(),
                        lastResult.topicStatus().cleanupPolicy(),
                        lastResult.listenerRunning(),
                        lastResult.metrics().received(),
                        lastResult.metrics().processed(),
                        lastResult.metrics().created(),
                        lastResult.metrics().updated(),
                        lastResult.metrics().duplicated(),
                        lastResult.metrics().stale(),
                        lastResult.metrics().functionalProbesIgnored(),
                        lastResult.metrics().failures(),
                        lastResult.localSnapshots(),
                        attempt
                );
                log.info(
                        "El selector de permisos ya puede obtener los empleados "
                                + "desde el snapshot local de MS3."
                );
                log.info(
                        "============================================================"
                );

                return;
            }

            log.warn(
                    "Reconciliación MS2 -> MS3 todavía no verificada. "
                            + "attempt={}/{}, topicAvailable={}, topicCompacted={}, "
                            + "partitions={}, listenerRunning={}, handledEvents={}, "
                            + "consumerFailures={}, localSnapshots={}, detail={}",
                    attempt,
                    properties.getMaxAttempts(),
                    lastResult.topicStatus().available(),
                    lastResult.topicStatus().compacted(),
                    lastResult.topicStatus().partitions(),
                    lastResult.listenerRunning(),
                    lastResult.metrics().handled(),
                    lastResult.metrics().failures(),
                    lastResult.localSnapshots(),
                    lastResult.detail()
            );

            if (attempt < properties.getMaxAttempts()) {
                sleepSafely(
                        properties.getRetryDelay()
                );
            }
        }

        String message =
                buildFailureMessage(
                        lastResult
                );

        log.error(
                "============================================================"
        );
        log.error(
                "NO SE PUDO VERIFICAR LA RECONCILIACIÓN MS2 -> MS3"
        );
        log.error(
                message
        );
        log.error(
                "Revisa Kafka, el topic {}, el consumidor {} y los logs de MS2.",
                properties.getTopic(),
                properties.getListenerId()
        );
        log.error(
                "============================================================"
        );

        if (properties.isFailFast()) {
            throw new IllegalStateException(
                    message
            );
        }
    }

    private VerificationResult verifyCurrentState(
            int attempt
    ) {
        TopicStatus topicStatus =
                inspectTopic();

        MessageListenerContainer listenerContainer =
                listenerRegistry.getListenerContainer(
                        properties.getListenerId()
                );

        boolean listenerRunning =
                listenerContainer != null
                        && listenerContainer.isRunning();

        Snapshot metrics =
                consumerMetrics.snapshot();

        long localSnapshots =
                empleadoSnapshotRepository.count();

        boolean consumedEventsSatisfied =
                metrics.handled()
                        >= properties.getMinimumConsumedEvents();

        boolean snapshotsSatisfied =
                localSnapshots
                        >= properties.getMinimumSnapshots();

        boolean successful =
                topicStatus.available()
                        && topicStatus.compacted()
                        && listenerRunning
                        && consumedEventsSatisfied
                        && snapshotsSatisfied
                        && metrics.failures() == 0;

        String detail =
                successful
                        ? "OK"
                        : buildDetail(
                        topicStatus,
                        listenerRunning,
                        consumedEventsSatisfied,
                        snapshotsSatisfied,
                        metrics
                );

        return new VerificationResult(
                attempt,
                topicStatus,
                listenerRunning,
                metrics,
                localSnapshots,
                successful,
                detail
        );
    }

    private TopicStatus inspectTopic() {
        String topic =
                properties.getTopic();

        long timeoutMillis =
                safeTimeoutMillis(
                        properties.getAdminTimeout()
                );

        try (
                AdminClient adminClient =
                        AdminClient.create(
                                kafkaAdmin
                                        .getConfigurationProperties()
                        )
        ) {
            Map<String, TopicDescription> descriptions =
                    adminClient.describeTopics(
                                    Set.of(
                                            topic
                                    )
                            )
                            .allTopicNames()
                            .get(
                                    timeoutMillis,
                                    TimeUnit.MILLISECONDS
                            );

            TopicDescription description =
                    descriptions.get(
                            topic
                    );

            if (description == null) {
                return TopicStatus.unavailable(
                        "Kafka no devolvió la descripción del topic."
                );
            }

            ConfigResource resource =
                    new ConfigResource(
                            ConfigResource.Type.TOPIC,
                            topic
                    );

            Map<ConfigResource, Config> configs =
                    adminClient.describeConfigs(
                                    List.of(
                                            resource
                                    )
                            )
                            .all()
                            .get(
                                    timeoutMillis,
                                    TimeUnit.MILLISECONDS
                            );

            Config topicConfig =
                    configs.get(
                            resource
                    );

            ConfigEntry cleanupEntry =
                    topicConfig == null
                            ? null
                            : topicConfig.get(
                            TopicConfig.CLEANUP_POLICY_CONFIG
                    );

            String cleanupPolicy =
                    cleanupEntry == null
                            ? null
                            : cleanupEntry.value();

            boolean compacted =
                    containsPolicy(
                            cleanupPolicy,
                            TopicConfig.CLEANUP_POLICY_COMPACT
                    );

            return new TopicStatus(
                    true,
                    compacted,
                    description.partitions()
                            .size(),
                    cleanupPolicy,
                    null
            );
        } catch (Exception ex) {
            return TopicStatus.unavailable(
                    safeMessage(ex)
            );
        }
    }

    private boolean containsPolicy(
            String policies,
            String expectedPolicy
    ) {
        if (
                policies == null
                        || policies.isBlank()
                        || expectedPolicy == null
                        || expectedPolicy.isBlank()
        ) {
            return false;
        }

        for (String policy : policies.split(",")) {
            if (
                    expectedPolicy.equalsIgnoreCase(
                            policy.trim()
                    )
            ) {
                return true;
            }
        }

        return false;
    }

    private String buildDetail(
            TopicStatus topicStatus,
            boolean listenerRunning,
            boolean consumedEventsSatisfied,
            boolean snapshotsSatisfied,
            Snapshot metrics
    ) {
        StringBuilder detail =
                new StringBuilder();

        if (!topicStatus.available()) {
            appendDetail(
                    detail,
                    "topic no disponible: "
                            + topicStatus.error()
            );
        } else if (!topicStatus.compacted()) {
            appendDetail(
                    detail,
                    "cleanup.policy del topic no contiene compact"
            );
        }

        if (!listenerRunning) {
            appendDetail(
                    detail,
                    "listener Kafka de empleados no está ejecutándose"
            );
        }

        if (!consumedEventsSatisfied) {
            appendDetail(
                    detail,
                    "eventos reales consumidos="
                            + metrics.handled()
                            + ", mínimo esperado="
                            + properties.getMinimumConsumedEvents()
            );
        }

        if (!snapshotsSatisfied) {
            appendDetail(
                    detail,
                    "snapshots locales insuficientes"
            );
        }

        if (metrics.failures() > 0) {
            appendDetail(
                    detail,
                    "errores del consumidor="
                            + metrics.failures()
            );
        }

        return detail.isEmpty()
                ? "estado incompleto"
                : detail.toString();
    }

    private void appendDetail(
            StringBuilder detail,
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return;
        }

        if (!detail.isEmpty()) {
            detail.append(
                    "; "
            );
        }

        detail.append(
                value
        );
    }

    private String buildFailureMessage(
            VerificationResult result
    ) {
        if (result == null) {
            return "No se obtuvo información durante la verificación.";
        }

        return "La reconciliación no alcanzó el estado esperado. "
                + "topicAvailable="
                + result.topicStatus().available()
                + ", topicCompacted="
                + result.topicStatus().compacted()
                + ", partitions="
                + result.topicStatus().partitions()
                + ", listenerRunning="
                + result.listenerRunning()
                + ", handledEvents="
                + result.metrics().handled()
                + ", consumerFailures="
                + result.metrics().failures()
                + ", localSnapshots="
                + result.localSnapshots()
                + ", detail="
                + result.detail();
    }

    private void sleepSafely(
            Duration duration
    ) {
        if (
                duration == null
                        || duration.isNegative()
                        || duration.isZero()
        ) {
            return;
        }

        try {
            Thread.sleep(
                    duration.toMillis()
            );
        } catch (InterruptedException ex) {
            Thread.currentThread()
                    .interrupt();

            throw new IllegalStateException(
                    "La verificación de reconciliación fue interrumpida.",
                    ex
            );
        }
    }

    private long safeTimeoutMillis(
            Duration duration
    ) {
        if (
                duration == null
                        || duration.isNegative()
                        || duration.isZero()
        ) {
            return 5000L;
        }

        return Math.max(
                1L,
                duration.toMillis()
        );
    }

    private String safeMessage(
            Throwable throwable
    ) {
        if (
                throwable == null
                        || throwable.getMessage() == null
                        || throwable.getMessage().isBlank()
        ) {
            return "error no especificado";
        }

        String message =
                throwable.getMessage()
                        .trim();

        return message.length() <= 500
                ? message
                : message.substring(
                0,
                500
        );
    }

    private record TopicStatus(
            boolean available,
            boolean compacted,
            int partitions,
            String cleanupPolicy,
            String error
    ) {

        private static TopicStatus unavailable(
                String error
        ) {
            return new TopicStatus(
                    false,
                    false,
                    0,
                    null,
                    error
            );
        }
    }

    private record VerificationResult(
            int attempt,
            TopicStatus topicStatus,
            boolean listenerRunning,
            Snapshot metrics,
            long localSnapshots,
            boolean successful,
            String detail
    ) {
    }
}
```

## Qué debes observar en la consola

En MS2:

```text
INICIANDO RECONCILIACIÓN AUTOMÁTICA DE EMPLEADOS MS2 -> MS3
Página de reconciliación de empleados registrada en Outbox
Publicando evento outbox a Kafka
RECONCILIACIÓN AUTOMÁTICA MS2 -> MS3 COMPLETADA
eventosRegistrados=1, publicados=1, pendientes=0, errores=0
```

En MS3:

```text
Evento de empleado MS2 atendido
processed=true
created=true
```

Y finalmente:

```text
RECONCILIACIÓN DE EMPLEADOS MS2 -> MS3 VERIFICADA
topic=ms2.empleado.snapshot.v1
cleanupPolicy=compact
listenerRunning=true
eventosProcesados=1
snapshotsLocales=1
```

Después de esos mensajes, esta consulta:

```http
GET /api/ms3/catalogo/lookups/empleados-inventario?soloActivos=true&limit=50
```

debe devolver el empleado bootstrap y el selector de permisos dejará de mostrar `0 resultados`.

No modifiques el Compose de Kafka: el tópico ya existe, tiene tres particiones, un factor de réplica adecuado para el broker local y política compactada. Tampoco actives `republish-snapshots-on-startup` del bootstrap: la nueva reconciliación global ya cubre todos los empleados y evita depender exclusivamente del empleado de prueba.

El código se preparó mediante revisión estática; no fue compilado ni ejecutado.
