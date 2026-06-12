package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.Categoria;
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

public interface CategoriaRepository
        extends
        JpaRepository<Categoria, Long>,
        JpaSpecificationExecutor<Categoria> {

    @EntityGraph(
            attributePaths = {
                    "categoriaPadre"
            }
    )
    @Override
    Optional<Categoria> findById(
            Long idCategoria
    );

    @EntityGraph(
            attributePaths = {
                    "categoriaPadre"
            }
    )
    @Override
    Page<Categoria> findAll(
            Specification<Categoria> specification,
            Pageable pageable
    );

    Optional<Categoria> findByIdCategoriaAndEstadoTrue(
            Long idCategoria
    );

    Optional<Categoria> findByCodigoIgnoreCaseAndEstadoTrue(
            String codigo
    );

    Optional<Categoria> findBySlugIgnoreCaseAndEstadoTrue(
            String slug
    );

    @EntityGraph(
            attributePaths = {
                    "categoriaPadre"
            }
    )
    List<Categoria> findByNombreIgnoreCaseAndEstadoTrueOrderByNivelAscOrdenAscIdCategoriaAsc(
            String nombre
    );

    boolean existsByCodigoIgnoreCase(
            String codigo
    );

    boolean existsBySlugIgnoreCase(
            String slug
    );

    boolean existsByCodigoIgnoreCaseAndEstadoTrueAndIdCategoriaNot(
            String codigo,
            Long idCategoria
    );

    boolean existsBySlugIgnoreCaseAndEstadoTrueAndIdCategoriaNot(
            String slug,
            Long idCategoria
    );

    @Query("""
            select case when count(c) > 0 then true else false end
            from Categoria c
            where c.estado = true
              and lower(c.nombre) = lower(:nombre)
              and (
                    (:idCategoriaPadre is null and c.categoriaPadre is null)
                    or c.categoriaPadre.idCategoria = :idCategoriaPadre
              )
            """)
    boolean existsActiveByNombreAndParent(
            @Param("nombre") String nombre,
            @Param("idCategoriaPadre") Long idCategoriaPadre
    );

    @Query("""
            select case when count(c) > 0 then true else false end
            from Categoria c
            where c.estado = true
              and c.idCategoria <> :idCategoria
              and lower(c.nombre) = lower(:nombre)
              and (
                    (:idCategoriaPadre is null and c.categoriaPadre is null)
                    or c.categoriaPadre.idCategoria = :idCategoriaPadre
              )
            """)
    boolean existsActiveByNombreAndParentExcludingId(
            @Param("nombre") String nombre,
            @Param("idCategoriaPadre") Long idCategoriaPadre,
            @Param("idCategoria") Long idCategoria
    );

    boolean existsByCategoriaPadre_IdCategoriaAndEstadoTrue(
            Long idCategoriaPadre
    );

    @EntityGraph(
            attributePaths = {
                    "categoriaPadre"
            }
    )
    List<Categoria> findByCategoriaPadre_IdCategoriaAndEstadoTrueOrderByOrdenAscNombreAsc(
            Long idCategoriaPadre
    );

    @EntityGraph(
            attributePaths = {
                    "categoriaPadre"
            }
    )
    List<Categoria> findByCategoriaPadre_IdCategoriaOrderByOrdenAscNombreAsc(
            Long idCategoriaPadre
    );

    @EntityGraph(
            attributePaths = {
                    "categoriaPadre"
            }
    )
    List<Categoria> findByEstadoTrueOrderByNivelAscOrdenAscNombreAsc();

    @EntityGraph(
            attributePaths = {
                    "categoriaPadre"
            }
    )
    List<Categoria> findAllByOrderByNivelAscOrdenAscNombreAsc();
}
