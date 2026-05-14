// ruta: src/main/java/com/upsjb/ms3/repository/ProductoAtributoValorRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.ProductoAtributoValor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductoAtributoValorRepository extends
        JpaRepository<ProductoAtributoValor, Long>,
        JpaSpecificationExecutor<ProductoAtributoValor> {

    Optional<ProductoAtributoValor> findByIdProductoAtributoValorAndEstadoTrue(Long idProductoAtributoValor);

    Optional<ProductoAtributoValor> findByProducto_IdProductoAndAtributo_IdAtributoAndEstadoTrue(
            Long idProducto,
            Long idAtributo
    );

    boolean existsByProducto_IdProductoAndAtributo_IdAtributoAndEstadoTrue(Long idProducto, Long idAtributo);

    boolean existsByProducto_IdProductoAndAtributo_IdAtributoAndEstadoTrueAndIdProductoAtributoValorNot(
            Long idProducto,
            Long idAtributo,
            Long idProductoAtributoValor
    );

    List<ProductoAtributoValor> findByProducto_IdProductoAndEstadoTrueOrderByIdProductoAtributoValorAsc(
            Long idProducto
    );

    List<ProductoAtributoValor> findByAtributo_IdAtributoAndEstadoTrueOrderByIdProductoAtributoValorAsc(
            Long idAtributo
    );

    long countByProducto_IdProductoAndEstadoTrue(Long idProducto);

    long countByAtributo_IdAtributoAndEstadoTrue(Long idAtributo);
}