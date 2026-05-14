// ruta: src/main/java/com/upsjb/ms3/repository/TipoProductoAtributoRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.TipoProductoAtributo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TipoProductoAtributoRepository extends
        JpaRepository<TipoProductoAtributo, Long>,
        JpaSpecificationExecutor<TipoProductoAtributo> {

    Optional<TipoProductoAtributo> findByIdTipoProductoAtributoAndEstadoTrue(Long idTipoProductoAtributo);

    Optional<TipoProductoAtributo> findByTipoProducto_IdTipoProductoAndAtributo_IdAtributoAndEstadoTrue(
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

    long countByAtributo_IdAtributoAndEstadoTrue(Long idAtributo);

    Page<TipoProductoAtributo> findByTipoProducto_IdTipoProductoAndEstadoTrue(
            Long idTipoProducto,
            Pageable pageable
    );

    List<TipoProductoAtributo> findByTipoProducto_IdTipoProductoAndEstadoTrueOrderByOrdenAscIdTipoProductoAtributoAsc(
            Long idTipoProducto
    );

    List<TipoProductoAtributo> findByAtributo_IdAtributoAndEstadoTrueOrderByIdTipoProductoAtributoAsc(
            Long idAtributo
    );
}