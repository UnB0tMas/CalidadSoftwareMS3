// ruta: src/main/java/com/upsjb/ms3/repository/SkuAtributoValorRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.SkuAtributoValor;
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

public interface SkuAtributoValorRepository extends
        JpaRepository<SkuAtributoValor, Long>,
        JpaSpecificationExecutor<SkuAtributoValor> {

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto",
            "atributo"
    })
    @Override
    Page<SkuAtributoValor> findAll(Specification<SkuAtributoValor> specification, Pageable pageable);

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto",
            "atributo"
    })
    Optional<SkuAtributoValor> findByIdSkuAtributoValor(Long idSkuAtributoValor);

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto",
            "atributo"
    })
    Optional<SkuAtributoValor> findByIdSkuAtributoValorAndEstadoTrue(Long idSkuAtributoValor);

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto",
            "atributo"
    })
    Optional<SkuAtributoValor> findBySku_IdSkuAndAtributo_IdAtributoAndEstadoTrue(
            Long idSku,
            Long idAtributo
    );

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto",
            "atributo"
    })
    Optional<SkuAtributoValor> findFirstBySku_IdSkuAndAtributo_IdAtributoOrderByIdSkuAtributoValorDesc(
            Long idSku,
            Long idAtributo
    );

    boolean existsBySku_IdSkuAndAtributo_IdAtributoAndEstadoTrue(
            Long idSku,
            Long idAtributo
    );

    boolean existsBySku_IdSkuAndAtributo_IdAtributoAndEstadoTrueAndIdSkuAtributoValorNot(
            Long idSku,
            Long idAtributo,
            Long idSkuAtributoValor
    );

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto",
            "atributo"
    })
    List<SkuAtributoValor> findBySku_IdSkuAndEstadoTrueOrderByIdSkuAtributoValorAsc(
            Long idSku
    );

    @EntityGraph(attributePaths = {
            "sku",
            "sku.producto",
            "atributo"
    })
    List<SkuAtributoValor> findByAtributo_IdAtributoAndEstadoTrueOrderByIdSkuAtributoValorAsc(
            Long idAtributo
    );

    long countBySku_IdSkuAndEstadoTrue(Long idSku);

    long countByAtributo_IdAtributoAndEstadoTrue(Long idAtributo);

    @Query("""
            select count(v)
            from SkuAtributoValor v
            where v.estado = true
              and v.atributo.idAtributo = :idAtributo
              and v.sku.producto.tipoProducto.idTipoProducto = :idTipoProducto
            """)
    long countByTipoProductoAndAtributoActivos(
            @Param("idTipoProducto") Long idTipoProducto,
            @Param("idAtributo") Long idAtributo
    );
}