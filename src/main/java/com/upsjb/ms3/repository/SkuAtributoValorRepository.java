// ruta: src/main/java/com/upsjb/ms3/repository/SkuAtributoValorRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.SkuAtributoValor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SkuAtributoValorRepository extends
        JpaRepository<SkuAtributoValor, Long>,
        JpaSpecificationExecutor<SkuAtributoValor> {

    Optional<SkuAtributoValor> findByIdSkuAtributoValorAndEstadoTrue(Long idSkuAtributoValor);

    Optional<SkuAtributoValor> findBySku_IdSkuAndAtributo_IdAtributoAndEstadoTrue(
            Long idSku,
            Long idAtributo
    );

    boolean existsBySku_IdSkuAndAtributo_IdAtributoAndEstadoTrue(Long idSku, Long idAtributo);

    boolean existsBySku_IdSkuAndAtributo_IdAtributoAndEstadoTrueAndIdSkuAtributoValorNot(
            Long idSku,
            Long idAtributo,
            Long idSkuAtributoValor
    );

    List<SkuAtributoValor> findBySku_IdSkuAndEstadoTrueOrderByIdSkuAtributoValorAsc(Long idSku);

    List<SkuAtributoValor> findByAtributo_IdAtributoAndEstadoTrueOrderByIdSkuAtributoValorAsc(Long idAtributo);

    long countBySku_IdSkuAndEstadoTrue(Long idSku);

    long countByAtributo_IdAtributoAndEstadoTrue(Long idAtributo);
}