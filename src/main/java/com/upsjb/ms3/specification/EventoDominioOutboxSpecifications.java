// ruta: src/main/java/com/upsjb/ms3/specification/EventoDominioOutboxSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.EventoDominioOutbox;
import com.upsjb.ms3.domain.enums.AggregateType;
import com.upsjb.ms3.domain.enums.EstadoPublicacionEvento;
import com.upsjb.ms3.dto.outbox.filter.EventoDominioOutboxFilterDto;
import com.upsjb.ms3.shared.specification.BooleanCriteria;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class EventoDominioOutboxSpecifications {

    private EventoDominioOutboxSpecifications() {
    }

    public static Specification<EventoDominioOutbox> fromFilter(EventoDominioOutboxFilterDto filter) {
        if (filter == null) {
            return activeOnly();
        }

        return SpecificationBuilder.<EventoDominioOutbox>create()
                .textSearch(
                        SpecificationFilterSupport.text(filter, "search"),
                        "aggregateId",
                        "eventType",
                        "topic",
                        "eventKey",
                        "payloadJson",
                        "errorPublicacion",
                        "lockedBy"
                )
                .equal("idEvento", SpecificationFilterSupport.longValue(filter, "idEvento"))
                .equal("eventId", resolveEventId(filter))
                .equal("aggregateType", SpecificationFilterSupport.value(filter, AggregateType.class, "aggregateType"))
                .like("aggregateId", SpecificationFilterSupport.text(filter, "aggregateId"))
                .like("eventType", SpecificationFilterSupport.text(filter, "eventType"))
                .like("topic", SpecificationFilterSupport.text(filter, "topic"))
                .like("eventKey", SpecificationFilterSupport.text(filter, "eventKey"))
                .equal("estadoPublicacion", SpecificationFilterSupport.value(filter, EstadoPublicacionEvento.class, "estadoPublicacion"))
                .like("lockedBy", SpecificationFilterSupport.text(filter, "lockedBy"))
                .bool("estado", BooleanCriteria.of(resolveEstado(filter)))
                .range("createdAt", SpecificationFilterSupport.dateRange(
                        SpecificationFilterSupport.value(filter, com.upsjb.ms3.dto.shared.DateRangeFilterDto.class, "fechaCreacion", "createdAt")
                ))
                .range("publishedAt", SpecificationFilterSupport.dateRange(
                        SpecificationFilterSupport.value(filter, com.upsjb.ms3.dto.shared.DateRangeFilterDto.class, "fechaPublicacion", "publishedAt")
                ))
                .range("lockedAt", SpecificationFilterSupport.dateRange(
                        SpecificationFilterSupport.value(filter, com.upsjb.ms3.dto.shared.DateRangeFilterDto.class, "fechaBloqueo", "lockedAt")
                ))
                .and(conError(SpecificationFilterSupport.bool(filter, "conError")))
                .and(bloqueadosOnly(SpecificationFilterSupport.bool(filter, "bloqueado", "locked")))
                .build();
    }

    public static Specification<EventoDominioOutbox> activeOnly() {
        return SpecificationBuilder.<EventoDominioOutbox>create()
                .equal("estado", Boolean.TRUE)
                .build();
    }

    public static Specification<EventoDominioOutbox> pendientesOnly() {
        return SpecificationBuilder.<EventoDominioOutbox>create()
                .equal("estado", Boolean.TRUE)
                .equal("estadoPublicacion", EstadoPublicacionEvento.PENDIENTE)
                .build();
    }

    public static Specification<EventoDominioOutbox> erroresOnly() {
        return SpecificationBuilder.<EventoDominioOutbox>create()
                .equal("estado", Boolean.TRUE)
                .equal("estadoPublicacion", EstadoPublicacionEvento.ERROR)
                .build();
    }

    public static Specification<EventoDominioOutbox> byAggregate(AggregateType aggregateType, String aggregateId) {
        return SpecificationBuilder.<EventoDominioOutbox>create()
                .equal("aggregateType", aggregateType)
                .like("aggregateId", aggregateId)
                .build();
    }

    public static Specification<EventoDominioOutbox> conError(Boolean conError) {
        return (root, query, cb) -> {
            if (conError == null) {
                return cb.conjunction();
            }

            if (Boolean.TRUE.equals(conError)) {
                return cb.and(
                        cb.isNotNull(root.get("errorPublicacion")),
                        cb.notEqual(root.get("errorPublicacion"), "")
                );
            }

            return cb.or(
                    cb.isNull(root.get("errorPublicacion")),
                    cb.equal(root.get("errorPublicacion"), "")
            );
        };
    }

    public static Specification<EventoDominioOutbox> bloqueadosOnly(Boolean bloqueado) {
        return (root, query, cb) -> {
            if (bloqueado == null) {
                return cb.conjunction();
            }

            if (Boolean.TRUE.equals(bloqueado)) {
                return cb.isNotNull(root.get("lockedAt"));
            }

            return cb.isNull(root.get("lockedAt"));
        };
    }

    private static Boolean resolveEstado(EventoDominioOutboxFilterDto filter) {
        Boolean estado = SpecificationFilterSupport.bool(filter, "estado");
        return estado == null ? Boolean.TRUE : estado;
    }

    private static UUID resolveEventId(EventoDominioOutboxFilterDto filter) {
        UUID eventId = SpecificationFilterSupport.value(filter, UUID.class, "eventId");

        if (eventId != null) {
            return eventId;
        }

        String value = SpecificationFilterSupport.text(filter, "eventId");

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}