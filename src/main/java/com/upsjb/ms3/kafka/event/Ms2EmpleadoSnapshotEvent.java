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