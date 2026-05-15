// ruta: src/main/java/com/upsjb/ms3/repository/PromocionRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.Promocion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PromocionRepository extends
        JpaRepository<Promocion, Long>,
        JpaSpecificationExecutor<Promocion> {

    Optional<Promocion> findByIdPromocionAndEstadoTrue(Long idPromocion);

    Optional<Promocion> findByCodigoIgnoreCaseAndEstadoTrue(String codigo);

    Optional<Promocion> findByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoIgnoreCaseAndEstadoTrue(String codigo);

    boolean existsByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoIgnoreCaseAndEstadoTrueAndIdPromocionNot(String codigo, Long idPromocion);

    boolean existsByNombreIgnoreCaseAndEstadoTrueAndIdPromocionNot(String nombre, Long idPromocion);

    Page<Promocion> findByEstadoTrue(Pageable pageable);

    Page<Promocion> findByCreadoPorIdUsuarioMs1AndEstadoTrue(Long creadoPorIdUsuarioMs1, Pageable pageable);

    List<Promocion> findByEstadoTrueOrderByNombreAsc();

    List<Promocion> findByEstadoTrueOrderByCreatedAtDesc();
}