package com.upsjb.ms3.kafka.consumer;

import com.upsjb.ms3.domain.entity.EmpleadoSnapshotMs2;
import com.upsjb.ms3.domain.entity.EventoEmpleadoMs2Consumido;
import com.upsjb.ms3.kafka.event.Ms2EmpleadoSnapshotEvent;
import com.upsjb.ms3.kafka.event.Ms2EmpleadoSnapshotEvent.Ms2EmpleadoSnapshotPayload;
import com.upsjb.ms3.repository.EmpleadoSnapshotMs2Repository;
import com.upsjb.ms3.repository.EventoEmpleadoMs2ConsumidoRepository;
import com.upsjb.ms3.util.StringNormalizer;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class Ms2EmpleadoSnapshotHandler {

    private static final int CODIGO_EMPLEADO_MAX_LENGTH = 50;
    private static final int NOMBRES_COMPLETOS_MAX_LENGTH = 250;
    private static final int AREA_CODIGO_MAX_LENGTH = 50;
    private static final int AREA_NOMBRE_MAX_LENGTH = 120;
    private static final int TOPIC_MAX_LENGTH = 200;
    private static final int EVENT_TYPE_MAX_LENGTH = 120;

    private final EmpleadoSnapshotMs2Repository empleadoSnapshotRepository;
    private final EventoEmpleadoMs2ConsumidoRepository eventoConsumidoRepository;

    @Transactional
    public Ms2EmpleadoSnapshotResult handle(
            ConsumerRecord<String, String> record,
            Ms2EmpleadoSnapshotEvent event
    ) {
        validateRecord(record);

        if (event == null) {
            throw new IllegalArgumentException(
                    "El evento de empleado MS2 es obligatorio."
            );
        }

        event.validate();

        if (eventoConsumidoRepository.existsByEventId(event.eventId())) {
            Long idEmpleadoSnapshot = empleadoSnapshotRepository
                    .findByIdEmpleadoMs2AndEstadoTrue(
                            event.data().idEmpleado()
                    )
                    .map(EmpleadoSnapshotMs2::getIdEmpleadoSnapshot)
                    .orElse(null);

            log.info(
                    "Evento de empleado MS2 duplicado. "
                            + "eventId={}, idEmpleadoMs2={}, topic={}, partition={}, offset={}",
                    event.eventId(),
                    event.data().idEmpleado(),
                    record.topic(),
                    record.partition(),
                    record.offset()
            );

            return Ms2EmpleadoSnapshotResult.duplicated(
                    event.eventId(),
                    idEmpleadoSnapshot,
                    event.data().idEmpleado()
            );
        }

        Ms2EmpleadoSnapshotPayload payload = event.data();

        String codigoEmpleado = StringNormalizer.clean(
                payload.codigoEmpleado()
        );

        String nombresCompletos = StringNormalizer.clean(
                event.nombresCompletos()
        );

        String areaCodigo = StringNormalizer.cleanOrNull(
                payload.areaCodigo()
        );

        String areaNombre = StringNormalizer.cleanOrNull(
                payload.areaNombre()
        );

        LocalDateTime snapshotAt = event.snapshotAt();

        validateNormalizedData(
                codigoEmpleado,
                nombresCompletos,
                areaCodigo,
                areaNombre,
                snapshotAt,
                record.topic(),
                event.eventType()
        );

        Optional<EmpleadoSnapshotMs2> existing = findExistingForUpdate(
                payload.idEmpleado(),
                payload.idUsuarioMs1(),
                codigoEmpleado
        );

        if (
                existing.isPresent()
                        && isStale(
                        existing.get().getSnapshotAt(),
                        snapshotAt
                )
        ) {
            EmpleadoSnapshotMs2 current = existing.get();

            saveConsumedEvent(
                    record,
                    event
            );

            log.info(
                    "Evento de empleado MS2 omitido por ser anterior al snapshot actual. "
                            + "eventId={}, idEmpleadoSnapshot={}, idEmpleadoMs2={}, "
                            + "snapshotActual={}, snapshotRecibido={}",
                    event.eventId(),
                    current.getIdEmpleadoSnapshot(),
                    current.getIdEmpleadoMs2(),
                    current.getSnapshotAt(),
                    snapshotAt
            );

            return Ms2EmpleadoSnapshotResult.stale(
                    event.eventId(),
                    current.getIdEmpleadoSnapshot(),
                    payload.idEmpleado()
            );
        }

        Long currentId = existing
                .map(EmpleadoSnapshotMs2::getIdEmpleadoSnapshot)
                .orElse(null);

        validateNoConflicts(
                currentId,
                payload.idEmpleado(),
                payload.idUsuarioMs1(),
                codigoEmpleado
        );

        boolean created = existing.isEmpty();

        EmpleadoSnapshotMs2 entity = existing.orElseGet(
                EmpleadoSnapshotMs2::new
        );

        entity.setIdEmpleadoMs2(
                payload.idEmpleado()
        );

        entity.setIdUsuarioMs1(
                payload.idUsuarioMs1()
        );

        entity.setCodigoEmpleado(
                codigoEmpleado
        );

        entity.setNombresCompletos(
                nombresCompletos
        );

        entity.setAreaCodigo(
                areaCodigo
        );

        entity.setAreaNombre(
                areaNombre
        );

        entity.setEmpleadoActivo(
                Boolean.TRUE.equals(payload.estado())
        );

        entity.setSnapshotVersion(
                event.snapshotVersion()
        );

        entity.setSnapshotAt(
                snapshotAt
        );

        /*
         * El registro del snapshot se conserva activo en MS3.
         * La situación laboral real se representa mediante
         * empleadoActivo.
         *
         * Así, una inactivación enviada por MS2 no elimina la
         * trazabilidad del snapshot, pero el empleado deja de
         * aparecer en los lookups que solicitan solo empleados activos.
         */
        entity.activar();

        EmpleadoSnapshotMs2 saved = empleadoSnapshotRepository.saveAndFlush(
                entity
        );

        saveConsumedEvent(
                record,
                event
        );

        log.info(
                "Snapshot de empleado MS2 procesado. "
                        + "eventId={}, idEmpleadoSnapshot={}, idEmpleadoMs2={}, "
                        + "idUsuarioMs1={}, codigoEmpleado={}, empleadoActivo={}, created={}",
                event.eventId(),
                saved.getIdEmpleadoSnapshot(),
                saved.getIdEmpleadoMs2(),
                saved.getIdUsuarioMs1(),
                saved.getCodigoEmpleado(),
                saved.getEmpleadoActivo(),
                created
        );

        return Ms2EmpleadoSnapshotResult.processed(
                event.eventId(),
                saved.getIdEmpleadoSnapshot(),
                saved.getIdEmpleadoMs2(),
                created
        );
    }

    private Optional<EmpleadoSnapshotMs2> findExistingForUpdate(
            Long idEmpleadoMs2,
            Long idUsuarioMs1,
            String codigoEmpleado
    ) {
        if (idEmpleadoMs2 != null) {
            Optional<EmpleadoSnapshotMs2> byEmpleado =
                    empleadoSnapshotRepository
                            .findActivoByIdEmpleadoMs2ForUpdate(
                                    idEmpleadoMs2
                            );

            if (byEmpleado.isPresent()) {
                return byEmpleado;
            }
        }

        if (idUsuarioMs1 != null) {
            Optional<EmpleadoSnapshotMs2> byUsuario =
                    empleadoSnapshotRepository
                            .findActivoByIdUsuarioMs1ForUpdate(
                                    idUsuarioMs1
                            );

            if (byUsuario.isPresent()) {
                return byUsuario;
            }
        }

        if (StringNormalizer.hasText(codigoEmpleado)) {
            return empleadoSnapshotRepository
                    .findActivoByCodigoEmpleadoForUpdate(
                            codigoEmpleado
                    );
        }

        return Optional.empty();
    }

    private void validateNoConflicts(
            Long currentId,
            Long idEmpleadoMs2,
            Long idUsuarioMs1,
            String codigoEmpleado
    ) {
        boolean duplicatedByEmpleado;

        boolean duplicatedByUsuario;

        boolean duplicatedByCodigo;

        if (currentId == null) {
            duplicatedByEmpleado =
                    empleadoSnapshotRepository
                            .existsByIdEmpleadoMs2AndEstadoTrue(
                                    idEmpleadoMs2
                            );

            duplicatedByUsuario =
                    empleadoSnapshotRepository
                            .existsByIdUsuarioMs1AndEstadoTrue(
                                    idUsuarioMs1
                            );

            duplicatedByCodigo =
                    empleadoSnapshotRepository
                            .existsByCodigoEmpleadoIgnoreCaseAndEstadoTrue(
                                    codigoEmpleado
                            );
        } else {
            duplicatedByEmpleado =
                    empleadoSnapshotRepository
                            .existsByIdEmpleadoMs2AndEstadoTrueAndIdEmpleadoSnapshotNot(
                                    idEmpleadoMs2,
                                    currentId
                            );

            duplicatedByUsuario =
                    empleadoSnapshotRepository
                            .existsByIdUsuarioMs1AndEstadoTrueAndIdEmpleadoSnapshotNot(
                                    idUsuarioMs1,
                                    currentId
                            );

            duplicatedByCodigo =
                    empleadoSnapshotRepository
                            .existsByCodigoEmpleadoIgnoreCaseAndEstadoTrueAndIdEmpleadoSnapshotNot(
                                    codigoEmpleado,
                                    currentId
                            );
        }

        if (duplicatedByEmpleado) {
            throw new IllegalArgumentException(
                    "Ya existe otro snapshot activo con el mismo idEmpleadoMs2."
            );
        }

        if (duplicatedByUsuario) {
            throw new IllegalArgumentException(
                    "Ya existe otro snapshot activo con el mismo idUsuarioMs1."
            );
        }

        if (duplicatedByCodigo) {
            throw new IllegalArgumentException(
                    "Ya existe otro snapshot activo con el mismo código de empleado."
            );
        }
    }

    private boolean isStale(
            LocalDateTime currentSnapshotAt,
            LocalDateTime incomingSnapshotAt
    ) {
        return currentSnapshotAt != null
                && incomingSnapshotAt != null
                && incomingSnapshotAt.isBefore(currentSnapshotAt);
    }

    private void saveConsumedEvent(
            ConsumerRecord<String, String> record,
            Ms2EmpleadoSnapshotEvent event
    ) {
        EventoEmpleadoMs2Consumido consumed =
                new EventoEmpleadoMs2Consumido();

        consumed.setEventId(
                event.eventId()
        );

        consumed.setTopic(
                record.topic()
        );

        consumed.setPartition(
                record.partition()
        );

        consumed.setKafkaOffset(
                record.offset()
        );

        consumed.setEventType(
                event.eventType().trim()
        );

        consumed.setAggregateId(
                event.aggregateId()
        );

        consumed.setOccurredAt(
                event.snapshotAt()
        );

        consumed.setConsumedAt(
                LocalDateTime.now()
        );

        eventoConsumidoRepository.save(
                consumed
        );
    }

    private void validateRecord(
            ConsumerRecord<String, String> record
    ) {
        if (record == null) {
            throw new IllegalArgumentException(
                    "El registro Kafka del empleado MS2 es obligatorio."
            );
        }

        requireText(
                record.topic(),
                "El topic Kafka del empleado MS2 es obligatorio."
        );

        requireMaxLength(
                record.topic(),
                TOPIC_MAX_LENGTH,
                "El nombre del topic Kafka supera 200 caracteres."
        );
    }

    private void validateNormalizedData(
            String codigoEmpleado,
            String nombresCompletos,
            String areaCodigo,
            String areaNombre,
            LocalDateTime snapshotAt,
            String topic,
            String eventType
    ) {
        requireText(
                codigoEmpleado,
                "El código del empleado MS2 es obligatorio."
        );

        requireMaxLength(
                codigoEmpleado,
                CODIGO_EMPLEADO_MAX_LENGTH,
                "El código del empleado MS2 no puede superar 50 caracteres."
        );

        requireText(
                nombresCompletos,
                "Los nombres completos del empleado MS2 son obligatorios."
        );

        requireMaxLength(
                nombresCompletos,
                NOMBRES_COMPLETOS_MAX_LENGTH,
                "Los nombres completos del empleado MS2 no pueden superar 250 caracteres."
        );

        requireMaxLength(
                areaCodigo,
                AREA_CODIGO_MAX_LENGTH,
                "El código de área del empleado MS2 no puede superar 50 caracteres."
        );

        requireMaxLength(
                areaNombre,
                AREA_NOMBRE_MAX_LENGTH,
                "El nombre de área del empleado MS2 no puede superar 120 caracteres."
        );

        if (snapshotAt == null) {
            throw new IllegalArgumentException(
                    "La fecha del snapshot de empleado MS2 es obligatoria."
            );
        }

        requireText(
                topic,
                "El topic Kafka del empleado MS2 es obligatorio."
        );

        requireText(
                eventType,
                "El eventType del empleado MS2 es obligatorio."
        );

        requireMaxLength(
                eventType,
                EVENT_TYPE_MAX_LENGTH,
                "El eventType del empleado MS2 no puede superar 120 caracteres."
        );
    }

    private void requireText(
            String value,
            String message
    ) {
        if (!StringNormalizer.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireMaxLength(
            String value,
            int maxLength,
            String message
    ) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(message);
        }
    }

    public record Ms2EmpleadoSnapshotResult(
            UUID eventId,
            Long idEmpleadoSnapshot,
            Long idEmpleadoMs2,
            boolean processed,
            boolean created,
            boolean duplicated,
            boolean stale
    ) {

        public static Ms2EmpleadoSnapshotResult processed(
                UUID eventId,
                Long idEmpleadoSnapshot,
                Long idEmpleadoMs2,
                boolean created
        ) {
            return new Ms2EmpleadoSnapshotResult(
                    eventId,
                    idEmpleadoSnapshot,
                    idEmpleadoMs2,
                    true,
                    created,
                    false,
                    false
            );
        }

        public static Ms2EmpleadoSnapshotResult duplicated(
                UUID eventId,
                Long idEmpleadoSnapshot,
                Long idEmpleadoMs2
        ) {
            return new Ms2EmpleadoSnapshotResult(
                    eventId,
                    idEmpleadoSnapshot,
                    idEmpleadoMs2,
                    false,
                    false,
                    true,
                    false
            );
        }

        public static Ms2EmpleadoSnapshotResult stale(
                UUID eventId,
                Long idEmpleadoSnapshot,
                Long idEmpleadoMs2
        ) {
            return new Ms2EmpleadoSnapshotResult(
                    eventId,
                    idEmpleadoSnapshot,
                    idEmpleadoMs2,
                    false,
                    false,
                    false,
                    true
            );
        }
    }
}