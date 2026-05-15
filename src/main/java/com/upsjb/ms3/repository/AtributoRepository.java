// ruta: src/main/java/com/upsjb/ms3/repository/AtributoRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AtributoRepository extends
        JpaRepository<Atributo, Long>,
        JpaSpecificationExecutor<Atributo> {

    Optional<Atributo> findByIdAtributoAndEstadoTrue(Long idAtributo);

    Optional<Atributo> findByCodigoIgnoreCaseAndEstadoTrue(String codigo);

    Optional<Atributo> findByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoIgnoreCaseAndEstadoTrue(String codigo);

    boolean existsByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoIgnoreCaseAndEstadoTrueAndIdAtributoNot(String codigo, Long idAtributo);

    boolean existsByNombreIgnoreCaseAndEstadoTrueAndIdAtributoNot(String nombre, Long idAtributo);

    Page<Atributo> findByEstadoTrue(Pageable pageable);

    List<Atributo> findByEstadoTrueOrderByNombreAsc();

    List<Atributo> findByFiltrableTrueAndEstadoTrueOrderByNombreAsc();

    List<Atributo> findByVisiblePublicoTrueAndEstadoTrueOrderByNombreAsc();

    List<Atributo> findByTipoDatoAndEstadoTrueOrderByNombreAsc(TipoDatoAtributo tipoDato);
}