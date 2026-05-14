// ruta: src/main/java/com/upsjb/ms3/repository/CategoriaRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoriaRepository extends
        JpaRepository<Categoria, Long>,
        JpaSpecificationExecutor<Categoria> {

    Optional<Categoria> findByIdCategoriaAndEstadoTrue(Long idCategoria);

    Optional<Categoria> findByCodigoIgnoreCaseAndEstadoTrue(String codigo);

    Optional<Categoria> findBySlugIgnoreCaseAndEstadoTrue(String slug);

    Optional<Categoria> findByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoIgnoreCaseAndEstadoTrue(String codigo);

    boolean existsBySlugIgnoreCaseAndEstadoTrue(String slug);

    boolean existsByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoIgnoreCaseAndEstadoTrueAndIdCategoriaNot(String codigo, Long idCategoria);

    boolean existsBySlugIgnoreCaseAndEstadoTrueAndIdCategoriaNot(String slug, Long idCategoria);

    boolean existsByNombreIgnoreCaseAndEstadoTrueAndIdCategoriaNot(String nombre, Long idCategoria);

    boolean existsByCategoriaPadre_IdCategoriaAndEstadoTrue(Long idCategoriaPadre);

    Page<Categoria> findByEstadoTrue(Pageable pageable);

    List<Categoria> findByEstadoTrueOrderByNivelAscOrdenAscNombreAsc();

    List<Categoria> findByCategoriaPadreIsNullAndEstadoTrueOrderByOrdenAscNombreAsc();

    List<Categoria> findByCategoriaPadre_IdCategoriaAndEstadoTrueOrderByOrdenAscNombreAsc(Long idCategoriaPadre);
}