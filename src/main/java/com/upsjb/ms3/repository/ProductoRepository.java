// ruta: src/main/java/com/upsjb/ms3/repository/ProductoRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductoRepository extends
        JpaRepository<Producto, Long>,
        JpaSpecificationExecutor<Producto> {

    Optional<Producto> findByIdProductoAndEstadoTrue(Long idProducto);

    Optional<Producto> findByCodigoProductoIgnoreCaseAndEstadoTrue(String codigoProducto);

    Optional<Producto> findBySlugIgnoreCaseAndEstadoTrue(String slug);

    Optional<Producto> findByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoProductoIgnoreCaseAndEstadoTrue(String codigoProducto);

    boolean existsBySlugIgnoreCaseAndEstadoTrue(String slug);

    boolean existsByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoProductoIgnoreCaseAndEstadoTrueAndIdProductoNot(String codigoProducto, Long idProducto);

    boolean existsBySlugIgnoreCaseAndEstadoTrueAndIdProductoNot(String slug, Long idProducto);

    boolean existsByNombreIgnoreCaseAndEstadoTrueAndIdProductoNot(String nombre, Long idProducto);

    boolean existsByTipoProducto_IdTipoProductoAndEstadoTrue(Long idTipoProducto);

    boolean existsByCategoria_IdCategoriaAndEstadoTrue(Long idCategoria);

    boolean existsByMarca_IdMarcaAndEstadoTrue(Long idMarca);

    long countByTipoProducto_IdTipoProductoAndEstadoTrue(Long idTipoProducto);

    long countByCategoria_IdCategoriaAndEstadoTrue(Long idCategoria);

    long countByMarca_IdMarcaAndEstadoTrue(Long idMarca);

    Page<Producto> findByEstadoTrue(Pageable pageable);

    Page<Producto> findByTipoProducto_IdTipoProductoAndEstadoTrue(Long idTipoProducto, Pageable pageable);

    Page<Producto> findByCategoria_IdCategoriaAndEstadoTrue(Long idCategoria, Pageable pageable);

    Page<Producto> findByMarca_IdMarcaAndEstadoTrue(Long idMarca, Pageable pageable);

    List<Producto> findByEstadoRegistroAndEstadoTrueOrderByUpdatedAtDesc(EstadoProductoRegistro estadoRegistro);

    List<Producto> findByEstadoPublicacionAndEstadoTrueOrderByUpdatedAtDesc(
            EstadoProductoPublicacion estadoPublicacion
    );

    List<Producto> findByEstadoVentaAndEstadoTrueOrderByUpdatedAtDesc(EstadoProductoVenta estadoVenta);

    Page<Producto> findByEstadoTrueAndVisiblePublicoTrueAndEstadoPublicacionInAndEstadoVentaIn(
            Collection<EstadoProductoPublicacion> estadosPublicacion,
            Collection<EstadoProductoVenta> estadosVenta,
            Pageable pageable
    );

    Optional<Producto> findBySlugIgnoreCaseAndEstadoTrueAndVisiblePublicoTrue(String slug);
}