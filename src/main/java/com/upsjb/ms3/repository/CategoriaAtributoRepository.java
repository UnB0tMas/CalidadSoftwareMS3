package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.CategoriaAtributo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoriaAtributoRepository
        extends
        JpaRepository<CategoriaAtributo, Long>,
        JpaSpecificationExecutor<CategoriaAtributo> {

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "atributo"
            }
    )
    @Override
    Page<CategoriaAtributo> findAll(
            Specification<CategoriaAtributo> specification,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "atributo"
            }
    )
    Optional<CategoriaAtributo> findByIdCategoriaAtributoAndEstadoTrue(
            Long idCategoriaAtributo
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "atributo"
            }
    )
    Optional<CategoriaAtributo> findByCategoria_IdCategoriaAndAtributo_IdAtributoAndEstadoTrue(
            Long idCategoria,
            Long idAtributo
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "atributo"
            }
    )
    Optional<CategoriaAtributo> findFirstByCategoria_IdCategoriaAndAtributo_IdAtributoOrderByIdCategoriaAtributoDesc(
            Long idCategoria,
            Long idAtributo
    );

    @Query("""
            select case when count(ca) > 0 then true else false end
            from CategoriaAtributo ca
            where ca.estado = true
              and ca.categoria.idCategoria = :idCategoria
              and ca.atributo.idAtributo = :idAtributo
            """)
    boolean existsDirectActiveAssociation(
            @Param("idCategoria") Long idCategoria,
            @Param("idAtributo") Long idAtributo
    );

    @Query(
            value = """
                    WITH categoria_jerarquia AS (
                        SELECT
                            c.id_categoria,
                            c.id_categoria_padre,
                            0 AS distancia
                        FROM categoria c
                        WHERE c.id_categoria = :idCategoria
                          AND c.estado = 1

                        UNION ALL

                        SELECT
                            padre.id_categoria,
                            padre.id_categoria_padre,
                            jerarquia.distancia + 1
                        FROM categoria padre
                        INNER JOIN categoria_jerarquia jerarquia
                            ON jerarquia.id_categoria_padre = padre.id_categoria
                        WHERE padre.estado = 1
                    )
                    SELECT CASE
                        WHEN EXISTS (
                            SELECT 1
                            FROM categoria_atributo ca
                            INNER JOIN categoria_jerarquia jerarquia
                                ON jerarquia.id_categoria = ca.id_categoria
                            INNER JOIN atributo a
                                ON a.id_atributo = ca.id_atributo
                            WHERE ca.estado = 1
                              AND a.estado = 1
                              AND ca.id_atributo = :idAtributo
                        )
                        THEN CAST(1 AS bit)
                        ELSE CAST(0 AS bit)
                    END
                    """,
            nativeQuery = true
    )
    boolean existsByCategoria_IdCategoriaAndAtributo_IdAtributoAndEstadoTrue(
            @Param("idCategoria") Long idCategoria,
            @Param("idAtributo") Long idAtributo
    );

    boolean existsByCategoria_IdCategoriaAndAtributo_IdAtributoAndEstadoTrueAndIdCategoriaAtributoNot(
            Long idCategoria,
            Long idAtributo,
            Long idCategoriaAtributo
    );

    long countByCategoria_IdCategoriaAndEstadoTrue(
            Long idCategoria
    );

    long countByAtributo_IdAtributoAndEstadoTrue(
            Long idAtributo
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "atributo"
            }
    )
    Page<CategoriaAtributo> findByCategoria_IdCategoriaAndEstadoTrue(
            Long idCategoria,
            Pageable pageable
    );

    @Query(
            value = """
                    WITH categoria_jerarquia AS (
                        SELECT
                            c.id_categoria,
                            c.id_categoria_padre,
                            0 AS distancia
                        FROM categoria c
                        WHERE c.id_categoria = :idCategoria
                          AND c.estado = 1

                        UNION ALL

                        SELECT
                            padre.id_categoria,
                            padre.id_categoria_padre,
                            jerarquia.distancia + 1
                        FROM categoria padre
                        INNER JOIN categoria_jerarquia jerarquia
                            ON jerarquia.id_categoria_padre = padre.id_categoria
                        WHERE padre.estado = 1
                    ),
                    atributos_priorizados AS (
                        SELECT
                            ca.id_categoria_atributo,
                            ca.id_categoria,
                            ca.id_atributo,
                            ca.requerido,
                            ca.orden,
                            ca.estado,
                            ca.created_at,
                            ca.updated_at,
                            ROW_NUMBER() OVER (
                                PARTITION BY ca.id_atributo
                                ORDER BY
                                    jerarquia.distancia ASC,
                                    ca.id_categoria_atributo DESC
                            ) AS prioridad
                        FROM categoria_atributo ca
                        INNER JOIN categoria_jerarquia jerarquia
                            ON jerarquia.id_categoria = ca.id_categoria
                        INNER JOIN atributo a
                            ON a.id_atributo = ca.id_atributo
                        WHERE ca.estado = 1
                          AND a.estado = 1
                    )
                    SELECT
                        id_categoria_atributo,
                        id_categoria,
                        id_atributo,
                        requerido,
                        orden,
                        estado,
                        created_at,
                        updated_at
                    FROM atributos_priorizados
                    WHERE prioridad = 1
                    ORDER BY orden ASC, id_categoria_atributo ASC
                    """,
            nativeQuery = true
    )
    List<CategoriaAtributo> findByCategoria_IdCategoriaAndEstadoTrueOrderByOrdenAscIdCategoriaAtributoAsc(
            @Param("idCategoria") Long idCategoria
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "atributo"
            }
    )
    List<CategoriaAtributo> findByAtributo_IdAtributoAndEstadoTrueOrderByIdCategoriaAtributoAsc(
            Long idAtributo
    );
}
