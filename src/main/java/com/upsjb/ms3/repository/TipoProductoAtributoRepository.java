// ruta: src/main/java/com/upsjb/ms3/repository/TipoProductoAtributoRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.TipoProductoAtributo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TipoProductoAtributoRepository extends
        JpaRepository<TipoProductoAtributo, Long>,
        JpaSpecificationExecutor<TipoProductoAtributo> {

    @EntityGraph(attributePaths = {
            "tipoProducto",
            "atributo"
    })
    @Override
    Page<TipoProductoAtributo> findAll(Specification<TipoProductoAtributo> specification, Pageable pageable);

    @EntityGraph(attributePaths = {
            "tipoProducto",
            "atributo"
    })
    Optional<TipoProductoAtributo> findByIdTipoProductoAtributoAndEstadoTrue(Long idTipoProductoAtributo);

    @EntityGraph(attributePaths = {
            "tipoProducto",
            "atributo"
    })
    Optional<TipoProductoAtributo> findByTipoProducto_IdTipoProductoAndAtributo_IdAtributoAndEstadoTrue(
            Long idTipoProducto,
            Long idAtributo
    );

    @EntityGraph(attributePaths = {
            "tipoProducto",
            "atributo"
    })
    Optional<TipoProductoAtributo> findFirstByTipoProducto_IdTipoProductoAndAtributo_IdAtributoOrderByIdTipoProductoAtributoDesc(
            Long idTipoProducto,
            Long idAtributo
    );

    boolean existsByTipoProducto_IdTipoProductoAndAtributo_IdAtributoAndEstadoTrue(
            Long idTipoProducto,
            Long idAtributo
    );

    boolean existsByTipoProducto_IdTipoProductoAndAtributo_IdAtributoAndEstadoTrueAndIdTipoProductoAtributoNot(
            Long idTipoProducto,
            Long idAtributo,
            Long idTipoProductoAtributo
    );

    long countByTipoProducto_IdTipoProductoAndEstadoTrue(Long idTipoProducto);

    long countByAtributo_IdAtributoAndEstadoTrue(Long idAtributo);

    @EntityGraph(attributePaths = {
            "tipoProducto",
            "atributo"
    })
    Page<TipoProductoAtributo> findByTipoProducto_IdTipoProductoAndEstadoTrue(
            Long idTipoProducto,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "tipoProducto",
            "atributo"
    })
    List<TipoProductoAtributo> findByTipoProducto_IdTipoProductoAndEstadoTrueOrderByOrdenAscIdTipoProductoAtributoAsc(
            Long idTipoProducto
    );

    @EntityGraph(attributePaths = {
            "tipoProducto",
            "atributo"
    })
    List<TipoProductoAtributo> findByAtributo_IdAtributoAndEstadoTrueOrderByIdTipoProductoAtributoAsc(
            Long idAtributo
    );
}