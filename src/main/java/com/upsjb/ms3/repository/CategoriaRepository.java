// ruta: src/main/java/com/upsjb/ms3/repository/CategoriaRepository.java
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

public interface CategoriaRepository extends
        JpaRepository<Categoria, Long>,
        JpaSpecificationExecutor<Categoria> {

    @EntityGraph(attributePaths = {
            "categoriaPadre"
    })
    @Override
    Optional<Categoria> findById(Long id);

    @EntityGraph(attributePaths = {
            "categoriaPadre"
    })
    @Override
    List<Categoria> findAll();

    @EntityGraph(attributePaths = {
            "categoriaPadre"
    })
    @Override
    Page<Categoria> findAll(Specification<Categoria> specification, Pageable pageable);

    @EntityGraph(attributePaths = {
            "categoriaPadre"
    })
    Optional<Categoria> findByIdCategoriaAndEstadoTrue(Long idCategoria);

    @EntityGraph(attributePaths = {
            "categoriaPadre"
    })
    Optional<Categoria> findByCodigoIgnoreCaseAndEstadoTrue(String codigo);

    @EntityGraph(attributePaths = {
            "categoriaPadre"
    })
    Optional<Categoria> findBySlugIgnoreCaseAndEstadoTrue(String slug);

    @EntityGraph(attributePaths = {
            "categoriaPadre"
    })
    Optional<Categoria> findByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoIgnoreCaseAndEstadoTrue(String codigo);

    boolean existsBySlugIgnoreCaseAndEstadoTrue(String slug);

    boolean existsByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoIgnoreCaseAndEstadoTrueAndIdCategoriaNot(String codigo, Long idCategoria);

    boolean existsBySlugIgnoreCaseAndEstadoTrueAndIdCategoriaNot(String slug, Long idCategoria);

    boolean existsByNombreIgnoreCaseAndEstadoTrueAndIdCategoriaNot(String nombre, Long idCategoria);

    boolean existsByCategoriaPadre_IdCategoriaAndEstadoTrue(Long idCategoriaPadre);

    @EntityGraph(attributePaths = {
            "categoriaPadre"
    })
    Page<Categoria> findByEstadoTrue(Pageable pageable);

    @EntityGraph(attributePaths = {
            "categoriaPadre"
    })
    List<Categoria> findAllByOrderByNivelAscOrdenAscNombreAsc();

    @EntityGraph(attributePaths = {
            "categoriaPadre"
    })
    List<Categoria> findByEstadoTrueOrderByNivelAscOrdenAscNombreAsc();

    @EntityGraph(attributePaths = {
            "categoriaPadre"
    })
    List<Categoria> findByCategoriaPadreIsNullAndEstadoTrueOrderByOrdenAscNombreAsc();

    @EntityGraph(attributePaths = {
            "categoriaPadre"
    })
    List<Categoria> findByCategoriaPadre_IdCategoriaAndEstadoTrueOrderByOrdenAscNombreAsc(Long idCategoriaPadre);
}