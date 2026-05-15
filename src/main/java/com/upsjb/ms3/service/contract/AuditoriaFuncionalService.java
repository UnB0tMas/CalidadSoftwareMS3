// ruta: src/main/java/com/upsjb/ms3/service/contract/AuditoriaFuncionalService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.auditoria.filter.AuditoriaFuncionalFilterDto;
import com.upsjb.ms3.dto.auditoria.response.AuditoriaFuncionalResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import java.util.Map;

public interface AuditoriaFuncionalService {

    void registrarExito(
            TipoEventoAuditoria tipoEvento,
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            String descripcion,
            Map<String, Object> metadata
    );

    void registrarFallo(
            TipoEventoAuditoria tipoEvento,
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            String descripcion,
            Map<String, Object> metadata
    );

    void registrarAccesoDenegado(
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            String descripcion,
            Map<String, Object> metadata
    );

    void registrarValidacionFallida(
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            String descripcion,
            Map<String, Object> metadata
    );

    void registrarErrorSistema(
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            String descripcion,
            Map<String, Object> metadata
    );

    ApiResponseDto<PageResponseDto<AuditoriaFuncionalResponseDto>> listar(
            AuditoriaFuncionalFilterDto filter,
            PageRequestDto pageRequest
    );

    ApiResponseDto<AuditoriaFuncionalResponseDto> obtenerDetalle(Long idAuditoria);
}