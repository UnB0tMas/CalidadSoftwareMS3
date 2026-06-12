// ruta: src/main/java/com/upsjb/ms3/repository/CorrelativoCodigoRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.CorrelativoCodigo;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CorrelativoCodigoRepository extends
        JpaRepository<CorrelativoCodigo, Long>,
        JpaSpecificationExecutor<CorrelativoCodigo> {

    Optional<CorrelativoCodigo> findByEntidadIgnoreCase(
            String entidad
    );

    Optional<CorrelativoCodigo> findByEntidadIgnoreCaseAndEstadoTrue(
            String entidad
    );

    boolean existsByEntidadIgnoreCaseAndEstadoTrue(
            String entidad
    );

    boolean existsByPrefijoIgnoreCaseAndEstadoTrue(
            String prefijo
    );

    @Modifying(
            flushAutomatically = true,
            clearAutomatically = false
    )
    @Query(
            value = """
                    MERGE correlativo_codigo WITH (HOLDLOCK) AS target
                    USING (
                        SELECT
                            :entidad AS entidad,
                            :prefijo AS prefijo,
                            :longitud AS longitud,
                            :descripcion AS descripcion
                    ) AS source
                    ON UPPER(target.entidad) = UPPER(source.entidad)
                    WHEN NOT MATCHED THEN
                        INSERT (
                            entidad,
                            prefijo,
                            ultimo_numero,
                            longitud,
                            descripcion,
                            estado,
                            created_at,
                            updated_at
                        )
                        VALUES (
                            source.entidad,
                            source.prefijo,
                            0,
                            source.longitud,
                            source.descripcion,
                            1,
                            SYSDATETIME(),
                            NULL
                        );
                    """,
            nativeQuery = true
    )
    int ensureExists(
            @Param("entidad") String entidad,
            @Param("prefijo") String prefijo,
            @Param("longitud") Integer longitud,
            @Param("descripcion") String descripcion
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from CorrelativoCodigo c
            where upper(c.entidad) = upper(:entidad)
              and c.estado = true
            """)
    Optional<CorrelativoCodigo> findActivoByEntidadForUpdate(
            @Param("entidad") String entidad
    );
}
