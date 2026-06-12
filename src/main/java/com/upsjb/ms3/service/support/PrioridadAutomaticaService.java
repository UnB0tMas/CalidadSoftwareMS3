package com.upsjb.ms3.service.support;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "ms3.prioridad-automatica",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class PrioridadAutomaticaService
        implements ApplicationRunner {

    private static final String PROMOTION_SESSION_KEY =
            "ms3_normaliza_promocion";

    private static final String IMAGE_SESSION_KEY =
            "ms3_normaliza_imagen";

    private static final String NORMALIZE_PROMOTIONS_SQL = """
            DECLARE @ahora datetime2(7) = SYSUTCDATETIME();

            ;WITH prioridades AS (
                SELECT
                    d.id_promocion_sku_descuento_version,
                    CAST(
                        ROW_NUMBER() OVER (
                            PARTITION BY d.id_sku
                            ORDER BY
                                CASE
                                    WHEN d.estado = 1
                                         AND pv.estado = 1
                                         AND p.estado = 1
                                         AND pv.vigente = 1
                                         AND pv.visible_publico = 1
                                         AND pv.estado_promocion IN (
                                             N'PROGRAMADA',
                                             N'ACTIVA'
                                         )
                                         AND pv.fecha_inicio <= @ahora
                                         AND pv.fecha_fin >= @ahora
                                    THEN 0

                                    WHEN d.estado = 1
                                         AND pv.estado = 1
                                         AND p.estado = 1
                                         AND pv.estado_promocion IN (
                                             N'PROGRAMADA',
                                             N'ACTIVA'
                                         )
                                         AND pv.fecha_inicio > @ahora
                                    THEN 1

                                    ELSE 2
                                END ASC,

                                pv.fecha_inicio DESC,
                                d.created_at DESC,
                                d.id_promocion_sku_descuento_version DESC
                        )
                        AS int
                    ) AS nueva_prioridad
                FROM dbo.promocion_sku_descuento_version AS d
                INNER JOIN dbo.promocion_version AS pv
                    ON pv.id_promocion_version =
                       d.id_promocion_version
                INNER JOIN dbo.promocion AS p
                    ON p.id_promocion =
                       pv.id_promocion
            )
            UPDATE destino
            SET destino.prioridad =
                    prioridades.nueva_prioridad
            FROM dbo.promocion_sku_descuento_version AS destino
            INNER JOIN prioridades
                ON prioridades.id_promocion_sku_descuento_version =
                   destino.id_promocion_sku_descuento_version
            WHERE ISNULL(
                      destino.prioridad,
                      0
                  ) <> prioridades.nueva_prioridad;
            """;

    private static final String NORMALIZE_IMAGES_SQL = """
            ;WITH ordenes AS (
                SELECT
                    i.id_imagen,
                    CAST(
                        ROW_NUMBER() OVER (
                            PARTITION BY
                                i.id_producto,
                                i.id_sku
                            ORDER BY
                                CASE
                                    WHEN i.estado = 1
                                    THEN 0
                                    ELSE 1
                                END ASC,

                                i.created_at DESC,
                                i.id_imagen DESC
                        )
                        AS int
                    ) AS nuevo_orden
                FROM dbo.producto_imagen_cloudinary AS i
            )
            UPDATE destino
            SET
                destino.orden =
                    ordenes.nuevo_orden,

                destino.principal =
                    CASE
                        WHEN destino.estado = 1
                             AND ordenes.nuevo_orden = 1
                        THEN CAST(1 AS bit)
                        ELSE CAST(0 AS bit)
                    END
            FROM dbo.producto_imagen_cloudinary AS destino
            INNER JOIN ordenes
                ON ordenes.id_imagen =
                   destino.id_imagen
            WHERE
                ISNULL(
                    destino.orden,
                    0
                ) <> ordenes.nuevo_orden

                OR ISNULL(
                    destino.principal,
                    0
                ) <> CASE
                         WHEN destino.estado = 1
                              AND ordenes.nuevo_orden = 1
                         THEN CAST(1 AS bit)
                         ELSE CAST(0 AS bit)
                     END;
            """;

    private static final String CREATE_PROMOTION_PROCEDURE_SQL =
            """
            CREATE OR ALTER PROCEDURE
                dbo.sp_ms3_normalizar_prioridades_promocion
            AS
            BEGIN
                SET NOCOUNT ON;

                IF TRY_CONVERT(
                    bit,
                    SESSION_CONTEXT(
                        N'ms3_normaliza_promocion'
                    )
                ) = 1
                BEGIN
                    RETURN;
                END;

                EXEC sys.sp_set_session_context
                    @key = N'ms3_normaliza_promocion',
                    @value = 1;

                BEGIN TRY
            """
                    + NORMALIZE_PROMOTIONS_SQL
                    + """
                    EXEC sys.sp_set_session_context
                        @key = N'ms3_normaliza_promocion',
                        @value = NULL;
                END TRY
                BEGIN CATCH
                    EXEC sys.sp_set_session_context
                        @key = N'ms3_normaliza_promocion',
                        @value = NULL;

                    THROW;
                END CATCH;
            END;
            """;

    private static final String CREATE_IMAGE_PROCEDURE_SQL =
            """
            CREATE OR ALTER PROCEDURE
                dbo.sp_ms3_normalizar_orden_imagenes
            AS
            BEGIN
                SET NOCOUNT ON;

                IF TRY_CONVERT(
                    bit,
                    SESSION_CONTEXT(
                        N'ms3_normaliza_imagen'
                    )
                ) = 1
                BEGIN
                    RETURN;
                END;

                EXEC sys.sp_set_session_context
                    @key = N'ms3_normaliza_imagen',
                    @value = 1;

                BEGIN TRY
            """
                    + NORMALIZE_IMAGES_SQL
                    + """
                    EXEC sys.sp_set_session_context
                        @key = N'ms3_normaliza_imagen',
                        @value = NULL;
                END TRY
                BEGIN CATCH
                    EXEC sys.sp_set_session_context
                        @key = N'ms3_normaliza_imagen',
                        @value = NULL;

                    THROW;
                END CATCH;
            END;
            """;

    private static final String CREATE_PROMOTION_DISCOUNT_TRIGGER_SQL =
            """
            CREATE OR ALTER TRIGGER
                dbo.trg_ms3_prioridad_promocion_descuento
            ON dbo.promocion_sku_descuento_version
            AFTER INSERT, UPDATE, DELETE
            AS
            BEGIN
                SET NOCOUNT ON;

                IF TRY_CONVERT(
                    bit,
                    SESSION_CONTEXT(
                        N'ms3_normaliza_promocion'
                    )
                ) = 1
                BEGIN
                    RETURN;
                END;

                EXEC dbo.sp_ms3_normalizar_prioridades_promocion;
            END;
            """;

    private static final String CREATE_PROMOTION_VERSION_TRIGGER_SQL =
            """
            CREATE OR ALTER TRIGGER
                dbo.trg_ms3_prioridad_promocion_version
            ON dbo.promocion_version
            AFTER INSERT, UPDATE, DELETE
            AS
            BEGIN
                SET NOCOUNT ON;

                IF TRY_CONVERT(
                    bit,
                    SESSION_CONTEXT(
                        N'ms3_normaliza_promocion'
                    )
                ) = 1
                BEGIN
                    RETURN;
                END;

                EXEC dbo.sp_ms3_normalizar_prioridades_promocion;
            END;
            """;

    private static final String CREATE_PROMOTION_TRIGGER_SQL =
            """
            CREATE OR ALTER TRIGGER
                dbo.trg_ms3_prioridad_promocion
            ON dbo.promocion
            AFTER INSERT, UPDATE, DELETE
            AS
            BEGIN
                SET NOCOUNT ON;

                IF TRY_CONVERT(
                    bit,
                    SESSION_CONTEXT(
                        N'ms3_normaliza_promocion'
                    )
                ) = 1
                BEGIN
                    RETURN;
                END;

                EXEC dbo.sp_ms3_normalizar_prioridades_promocion;
            END;
            """;

    private static final String CREATE_IMAGE_TRIGGER_SQL =
            """
            CREATE OR ALTER TRIGGER
                dbo.trg_ms3_orden_producto_imagen
            ON dbo.producto_imagen_cloudinary
            AFTER INSERT, UPDATE, DELETE
            AS
            BEGIN
                SET NOCOUNT ON;

                IF TRY_CONVERT(
                    bit,
                    SESSION_CONTEXT(
                        N'ms3_normaliza_imagen'
                    )
                ) = 1
                BEGIN
                    RETURN;
                END;

                EXEC dbo.sp_ms3_normalizar_orden_imagenes;
            END;
            """;

    private static final List<String> DATABASE_OBJECTS =
            List.of(
                    CREATE_PROMOTION_PROCEDURE_SQL,
                    CREATE_IMAGE_PROCEDURE_SQL,
                    CREATE_PROMOTION_DISCOUNT_TRIGGER_SQL,
                    CREATE_PROMOTION_VERSION_TRIGGER_SQL,
                    CREATE_PROMOTION_TRIGGER_SQL,
                    CREATE_IMAGE_TRIGGER_SQL
            );

    private final JdbcTemplate jdbcTemplate;

    private final TransactionTemplate transactionTemplate;

    private final AtomicBoolean normalizing =
            new AtomicBoolean(
                    false
            );

    @Value(
            "${ms3.prioridad-automatica.instalar-objetos-db:true}"
    )
    private boolean installDatabaseObjects;

    private volatile boolean databaseObjectsAvailable;

    @Override
    public void run(
            ApplicationArguments args
    ) {
        if (installDatabaseObjects) {
            installDatabaseObjects();
        }

        normalize(
                "inicio-aplicacion",
                false
        );
    }

    @Scheduled(
            initialDelayString =
                    "${ms3.prioridad-automatica.retraso-inicial-ms:60000}",

            fixedDelayString =
                    "${ms3.prioridad-automatica.intervalo-ms:60000}"
    )
    public void normalizeScheduled() {
        normalize(
                "programado",
                false
        );
    }

    private void installDatabaseObjects() {
        try {
            for (
                    String databaseObject
                    : DATABASE_OBJECTS
            ) {
                jdbcTemplate.execute(
                        databaseObject
                );
            }

            databaseObjectsAvailable =
                    true;

            log.info(
                    "Objetos SQL de prioridad automática instalados correctamente."
            );
        } catch (
                RuntimeException exception
        ) {
            databaseObjectsAvailable =
                    false;

            log.warn(
                    "No se pudieron instalar todos los procedimientos "
                            + "y triggers de prioridad automática. "
                            + "Se utilizará la normalización directa desde MS3.",
                    exception
            );
        }
    }

    private void normalize(
            String source,
            boolean propagateError
    ) {
        if (
                !normalizing.compareAndSet(
                        false,
                        true
                )
        ) {
            return;
        }

        try {
            if (databaseObjectsAvailable) {
                try {
                    executeInTransaction(
                            this::executeStoredProcedures
                    );

                    return;
                } catch (
                        RuntimeException exception
                ) {
                    databaseObjectsAvailable =
                            false;

                    log.warn(
                            "Los procedimientos de prioridad automática "
                                    + "no pudieron ejecutarse. "
                                    + "Se utilizará la normalización directa.",
                            exception
                    );
                }
            }

            executeInTransaction(
                    this::executeDirectNormalization
            );
        } catch (
                RuntimeException exception
        ) {
            log.error(
                    "No se pudo normalizar la prioridad automática. "
                            + "origen={}",
                    source,
                    exception
            );

            if (propagateError) {
                throw exception;
            }
        } finally {
            normalizing.set(
                    false
            );
        }
    }

    private void executeInTransaction(
            Runnable operation
    ) {
        transactionTemplate.executeWithoutResult(
                status ->
                        operation.run()
        );
    }

    private void executeStoredProcedures() {
        jdbcTemplate.execute(
                "EXEC dbo.sp_ms3_normalizar_prioridades_promocion"
        );

        jdbcTemplate.execute(
                "EXEC dbo.sp_ms3_normalizar_orden_imagenes"
        );
    }

    private void executeDirectNormalization() {
        setSessionContext(
                PROMOTION_SESSION_KEY,
                true
        );

        setSessionContext(
                IMAGE_SESSION_KEY,
                true
        );

        try {
            jdbcTemplate.execute(
                    NORMALIZE_PROMOTIONS_SQL
            );

            jdbcTemplate.execute(
                    NORMALIZE_IMAGES_SQL
            );
        } finally {
            clearSessionContextSafely(
                    IMAGE_SESSION_KEY
            );

            clearSessionContextSafely(
                    PROMOTION_SESSION_KEY
            );
        }
    }

    private void setSessionContext(
            String key,
            boolean enabled
    ) {
        String value =
                enabled
                        ? "1"
                        : "NULL";

        jdbcTemplate.execute(
                "EXEC sys.sp_set_session_context "
                        + "@key = N'"
                        + key
                        + "', @value = "
                        + value
        );
    }

    private void clearSessionContextSafely(
            String key
    ) {
        try {
            setSessionContext(
                    key,
                    false
            );
        } catch (
                RuntimeException exception
        ) {
            log.warn(
                    "No se pudo limpiar SESSION_CONTEXT para la clave {}.",
                    key,
                    exception
            );
        }
    }
}