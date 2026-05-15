// ruta: src/main/java/com/upsjb/ms3/specification/AuditoriaFuncionalSpecifications.java
package com.upsjb.ms3.specification;

import com.upsjb.ms3.domain.entity.AuditoriaFuncional;
import com.upsjb.ms3.dto.auditoria.filter.AuditoriaFuncionalFilterDto;
import com.upsjb.ms3.shared.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;

public final class AuditoriaFuncionalSpecifications {

    private AuditoriaFuncionalSpecifications() {
    }

    public static Specification<AuditoriaFuncional> fromFilter(AuditoriaFuncionalFilterDto filter) {
        if (filter == null) {
            return all();
        }

        return SpecificationBuilder.<AuditoriaFuncional>create()
                .equal("idUsuarioActorMs1", filter.idUsuarioActorMs1())
                .equal("idEmpleadoActorMs2", filter.idEmpleadoActorMs2())
                .equal("rolActor", filter.rolActor())
                .equal("tipoEvento", filter.tipoEvento())
                .equal("entidad", filter.entidad())
                .like("idRegistroAfectado", filter.idRegistroAfectado())
                .like("accion", filter.accion())
                .equal("resultado", filter.resultado())
                .like("requestId", filter.requestId())
                .like("correlationId", filter.correlationId())
                .like("ipAddress", filter.ipAddress())
                .range("eventAt", SpecificationFilterSupport.dateRange(filter.fechaEvento()))
                .build();
    }

    public static Specification<AuditoriaFuncional> all() {
        return SpecificationBuilder.<AuditoriaFuncional>create()
                .build();
    }

    public static Specification<AuditoriaFuncional> byRegistroAuditado(
            com.upsjb.ms3.domain.enums.EntidadAuditada entidad,
            String idRegistroAfectado
    ) {
        return SpecificationBuilder.<AuditoriaFuncional>create()
                .equal("entidad", entidad)
                .like("idRegistroAfectado", idRegistroAfectado)
                .build();
    }

    public static Specification<AuditoriaFuncional> byTrace(String requestId, String correlationId) {
        return SpecificationBuilder.<AuditoriaFuncional>create()
                .like("requestId", requestId)
                .like("correlationId", correlationId)
                .build();
    }
}