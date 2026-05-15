// ruta: src/main/java/com/upsjb/ms3/repository/ProductoAtributoValorRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.ProductoAtributoValor;
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

public interface ProductoAtributoValorRepository extends
        JpaRepository<ProductoAtributoValor, Long>,
        JpaSpecificationExecutor<ProductoAtributoValor> {

    @EntityGraph(attributePaths = {
            "producto",
            "producto.tipoProducto",
            "producto.categoria",
            "producto.marca",
            "atributo"
    })
    @Override
    Page<ProductoAtributoValor> findAll(
            Specification<ProductoAtributoValor> specification,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "producto",
            "producto.tipoProducto",
            "producto.categoria",
            "producto.marca",
            "atributo"
    })
    Optional<ProductoAtributoValor> findByIdProductoAtributoValor(Long idProductoAtributoValor);

    @EntityGraph(attributePaths = {
            "producto",
            "producto.tipoProducto",
            "producto.categoria",
            "producto.marca",
            "atributo"
    })
    Optional<ProductoAtributoValor> findByIdProductoAtributoValorAndEstadoTrue(Long idProductoAtributoValor);

    @EntityGraph(attributePaths = {
            "producto",
            "producto.tipoProducto",
            "producto.categoria",
            "producto.marca",
            "atributo"
    })
    Optional<ProductoAtributoValor> findByProducto_IdProductoAndAtributo_IdAtributoAndEstadoTrue(
            Long idProducto,
            Long idAtributo
    );

    boolean existsByProducto_IdProductoAndAtributo_IdAtributoAndEstadoTrue(
            Long idProducto,
            Long idAtributo
    );

    boolean existsByProducto_IdProductoAndAtributo_IdAtributoAndEstadoTrueAndIdProductoAtributoValorNot(
            Long idProducto,
            Long idAtributo,
            Long idProductoAtributoValor
    );

    @EntityGraph(attributePaths = {
            "producto",
            "producto.tipoProducto",
            "producto.categoria",
            "producto.marca",
            "atributo"
    })
    List<ProductoAtributoValor> findByProducto_IdProductoAndEstadoTrueOrderByIdProductoAtributoValorAsc(
            Long idProducto
    );

    @EntityGraph(attributePaths = {
            "producto",
            "producto.tipoProducto",
            "producto.categoria",
            "producto.marca",
            "atributo"
    })
    List<ProductoAtributoValor> findByAtributo_IdAtributoAndEstadoTrueOrderByIdProductoAtributoValorAsc(
            Long idAtributo
    );

    long countByProducto_IdProductoAndEstadoTrue(Long idProducto);

    long countByAtributo_IdAtributoAndEstadoTrue(Long idAtributo);

    @Query("""
            select count(v)
            from ProductoAtributoValor v
            where v.estado = true
              and v.atributo.idAtributo = :idAtributo
              and v.producto.tipoProducto.idTipoProducto = :idTipoProducto
            """)
    long countByTipoProductoAndAtributoActivos(
            @Param("idTipoProducto") Long idTipoProducto,
            @Param("idAtributo") Long idAtributo
    );
}