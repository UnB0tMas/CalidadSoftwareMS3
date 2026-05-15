// ruta: src/main/java/com/upsjb/ms3/repository/AuditoriaFuncionalRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.AuditoriaFuncional;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.ResultadoAuditoria;
import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditoriaFuncionalRepository extends
        JpaRepository<AuditoriaFuncional, Long>,
        JpaSpecificationExecutor<AuditoriaFuncional> {

    Page<AuditoriaFuncional> findByIdUsuarioActorMs1(Long idUsuarioActorMs1, Pageable pageable);

    Page<AuditoriaFuncional> findByIdEmpleadoActorMs2(Long idEmpleadoActorMs2, Pageable pageable);

    Page<AuditoriaFuncional> findByRolActor(RolSistema rolActor, Pageable pageable);

    Page<AuditoriaFuncional> findByTipoEvento(TipoEventoAuditoria tipoEvento, Pageable pageable);

    Page<AuditoriaFuncional> findByEntidad(EntidadAuditada entidad, Pageable pageable);

    Page<AuditoriaFuncional> findByResultado(ResultadoAuditoria resultado, Pageable pageable);

    Page<AuditoriaFuncional> findByEntidadAndIdRegistroAfectado(
            EntidadAuditada entidad,
            String idRegistroAfectado,
            Pageable pageable
    );

    Page<AuditoriaFuncional> findByRequestId(String requestId, Pageable pageable);

    Page<AuditoriaFuncional> findByCorrelationId(String correlationId, Pageable pageable);

    Page<AuditoriaFuncional> findByEventAtBetween(LocalDateTime desde, LocalDateTime hasta, Pageable pageable);

    List<AuditoriaFuncional> findByEntidadAndIdRegistroAfectadoOrderByEventAtDescIdAuditoriaDesc(
            EntidadAuditada entidad,
            String idRegistroAfectado
    );

    List<AuditoriaFuncional> findByRequestIdOrderByEventAtDescIdAuditoriaDesc(String requestId);

    List<AuditoriaFuncional> findByCorrelationIdOrderByEventAtDescIdAuditoriaDesc(String correlationId);
}