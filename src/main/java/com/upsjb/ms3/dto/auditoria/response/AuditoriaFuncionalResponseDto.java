// ruta: src/main/java/com/upsjb/ms3/dto/auditoria/response/AuditoriaFuncionalResponseDto.java
package com.upsjb.ms3.dto.auditoria.response;

import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.ResultadoAuditoria;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record AuditoriaFuncionalResponseDto(
        Long idAuditoria,
        Long idUsuarioActorMs1,
        Long idEmpleadoActorMs2,
        RolSistema rolActor,
        TipoEventoAuditoria tipoEvento,
        EntidadAuditada entidad,
        String idRegistroAfectado,
        String accion,
        ResultadoAuditoria resultado,
        String descripcion,
        String metadataJson,
        String ipAddress,
        String userAgent,
        String requestId,
        String correlationId,
        LocalDateTime eventAt
) {
}