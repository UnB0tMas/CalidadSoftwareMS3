// ruta: src/main/java/com/upsjb/ms3/dto/auditoria/filter/AuditoriaFuncionalFilterDto.java
package com.upsjb.ms3.dto.auditoria.filter;

import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.ResultadoAuditoria;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AuditoriaFuncionalFilterDto(

        Long idUsuarioActorMs1,

        Long idEmpleadoActorMs2,

        RolSistema rolActor,

        TipoEventoAuditoria tipoEvento,

        EntidadAuditada entidad,

        @Size(max = 100, message = "El id del registro afectado no debe superar 100 caracteres.")
        String idRegistroAfectado,

        @Size(max = 120, message = "La acción no debe superar 120 caracteres.")
        String accion,

        ResultadoAuditoria resultado,

        @Size(max = 100, message = "El requestId no debe superar 100 caracteres.")
        String requestId,

        @Size(max = 100, message = "El correlationId no debe superar 100 caracteres.")
        String correlationId,

        @Size(max = 80, message = "La IP no debe superar 80 caracteres.")
        String ipAddress,

        @Valid
        DateRangeFilterDto fechaEvento
) {
}