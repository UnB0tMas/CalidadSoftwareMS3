// ruta: src/main/java/com/upsjb/ms3/repository/CorrelativoCodigoRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.CorrelativoCodigo;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CorrelativoCodigoRepository extends
        JpaRepository<CorrelativoCodigo, Long>,
        JpaSpecificationExecutor<CorrelativoCodigo> {

    Optional<CorrelativoCodigo> findByEntidadIgnoreCaseAndEstadoTrue(String entidad);

    boolean existsByEntidadIgnoreCaseAndEstadoTrue(String entidad);

    boolean existsByPrefijoIgnoreCaseAndEstadoTrue(String prefijo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from CorrelativoCodigo c
            where upper(c.entidad) = upper(:entidad)
              and c.estado = true
            """)
    Optional<CorrelativoCodigo> findActivoByEntidadForUpdate(@Param("entidad") String entidad);
}