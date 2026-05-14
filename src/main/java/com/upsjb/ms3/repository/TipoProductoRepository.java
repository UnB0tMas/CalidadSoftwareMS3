// ruta: src/main/java/com/upsjb/ms3/repository/TipoProductoRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.TipoProducto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TipoProductoRepository extends
        JpaRepository<TipoProducto, Long>,
        JpaSpecificationExecutor<TipoProducto> {

    Optional<TipoProducto> findByIdTipoProductoAndEstadoTrue(Long idTipoProducto);

    Optional<TipoProducto> findByCodigoIgnoreCaseAndEstadoTrue(String codigo);

    Optional<TipoProducto> findByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoIgnoreCaseAndEstadoTrue(String codigo);

    boolean existsByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoIgnoreCaseAndEstadoTrueAndIdTipoProductoNot(String codigo, Long idTipoProducto);

    boolean existsByNombreIgnoreCaseAndEstadoTrueAndIdTipoProductoNot(String nombre, Long idTipoProducto);

    Page<TipoProducto> findByEstadoTrue(Pageable pageable);

    List<TipoProducto> findByEstadoTrueOrderByNombreAsc();
}