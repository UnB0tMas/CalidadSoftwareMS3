// ruta: src/main/java/com/upsjb/ms3/repository/MarcaRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.Marca;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MarcaRepository extends
        JpaRepository<Marca, Long>,
        JpaSpecificationExecutor<Marca> {

    Optional<Marca> findByIdMarcaAndEstadoTrue(Long idMarca);

    Optional<Marca> findByCodigoIgnoreCaseAndEstadoTrue(String codigo);

    Optional<Marca> findBySlugIgnoreCaseAndEstadoTrue(String slug);

    Optional<Marca> findByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoIgnoreCaseAndEstadoTrue(String codigo);

    boolean existsBySlugIgnoreCaseAndEstadoTrue(String slug);

    boolean existsByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoIgnoreCaseAndEstadoTrueAndIdMarcaNot(String codigo, Long idMarca);

    boolean existsBySlugIgnoreCaseAndEstadoTrueAndIdMarcaNot(String slug, Long idMarca);

    boolean existsByNombreIgnoreCaseAndEstadoTrueAndIdMarcaNot(String nombre, Long idMarca);

    Page<Marca> findByEstadoTrue(Pageable pageable);

    List<Marca> findByEstadoTrueOrderByNombreAsc();
}