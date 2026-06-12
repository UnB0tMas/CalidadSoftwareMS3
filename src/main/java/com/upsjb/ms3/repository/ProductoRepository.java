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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductoRepository extends
        JpaRepository<Producto, Long>,
        JpaSpecificationExecutor<Producto> {

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    @Override
    Optional<Producto> findById(
            Long idProducto
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    @Override
    Page<Producto> findAll(
            Specification<Producto> specification,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    Optional<Producto> findByIdProductoAndEstadoTrue(
            Long idProducto
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    Optional<Producto> findByCodigoProductoIgnoreCaseAndEstadoTrue(
            String codigoProducto
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    Optional<Producto> findBySlugIgnoreCaseAndEstadoTrue(
            String slug
    );

    Optional<Producto> findByNombreIgnoreCaseAndEstadoTrue(
            String nombre
    );

    boolean existsByCodigoProductoIgnoreCaseAndEstadoTrue(
            String codigoProducto
    );

    boolean existsBySlugIgnoreCaseAndEstadoTrue(
            String slug
    );

    boolean existsByNombreIgnoreCaseAndEstadoTrue(
            String nombre
    );

    boolean existsByCodigoProductoIgnoreCaseAndEstadoTrueAndIdProductoNot(
            String codigoProducto,
            Long idProducto
    );

    boolean existsBySlugIgnoreCaseAndEstadoTrueAndIdProductoNot(
            String slug,
            Long idProducto
    );

    boolean existsByNombreIgnoreCaseAndEstadoTrueAndIdProductoNot(
            String nombre,
            Long idProducto
    );

    boolean existsByCategoria_IdCategoriaAndEstadoTrue(
            Long idCategoria
    );

    boolean existsByMarca_IdMarcaAndEstadoTrue(
            Long idMarca
    );

    long countByCategoria_IdCategoriaAndEstadoTrue(
            Long idCategoria
    );

    long countByMarca_IdMarcaAndEstadoTrue(
            Long idMarca
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    Page<Producto> findByEstadoTrue(
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    Page<Producto> findByCategoria_IdCategoriaAndEstadoTrue(
            Long idCategoria,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    List<Producto> findByCategoria_IdCategoriaAndEstadoTrueOrderByIdProductoAsc(
            Long idCategoria
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    List<Producto> findByCategoria_IdCategoriaOrderByIdProductoAsc(
            Long idCategoria
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    List<Producto> findByCategoria_IdCategoriaInAndEstadoTrueOrderByIdProductoAsc(
            Collection<Long> idCategorias
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    List<Producto> findByCategoria_IdCategoriaInOrderByIdProductoAsc(
            Collection<Long> idCategorias
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    Page<Producto> findByMarca_IdMarcaAndEstadoTrue(
            Long idMarca,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    List<Producto> findByMarca_IdMarcaAndEstadoTrueOrderByIdProductoAsc(
            Long idMarca
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    List<Producto> findByMarca_IdMarcaOrderByIdProductoAsc(
            Long idMarca
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    List<Producto> findByEstadoRegistroAndEstadoTrueOrderByUpdatedAtDesc(
            EstadoProductoRegistro estadoRegistro
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    List<Producto> findByEstadoPublicacionAndEstadoTrueOrderByUpdatedAtDesc(
            EstadoProductoPublicacion estadoPublicacion
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    List<Producto> findByEstadoVentaAndEstadoTrueOrderByUpdatedAtDesc(
            EstadoProductoVenta estadoVenta
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    Page<Producto> findByEstadoTrueAndVisiblePublicoTrueAndEstadoPublicacionInAndEstadoVentaIn(
            Collection<EstadoProductoPublicacion> estadosPublicacion,
            Collection<EstadoProductoVenta> estadosVenta,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "categoria",
                    "categoria.categoriaPadre",
                    "marca"
            }
    )
    Optional<Producto> findBySlugIgnoreCaseAndEstadoTrueAndVisiblePublicoTrue(
            String slug
    );
}