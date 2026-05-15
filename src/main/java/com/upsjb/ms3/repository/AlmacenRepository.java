// ruta: src/main/java/com/upsjb/ms3/repository/AlmacenRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.Almacen;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AlmacenRepository extends
        JpaRepository<Almacen, Long>,
        JpaSpecificationExecutor<Almacen> {

    Optional<Almacen> findByIdAlmacenAndEstadoTrue(Long idAlmacen);

    Optional<Almacen> findByCodigoIgnoreCaseAndEstadoTrue(String codigo);

    Optional<Almacen> findByNombreIgnoreCaseAndEstadoTrue(String nombre);

    Optional<Almacen> findByPrincipalTrueAndEstadoTrue();

    boolean existsByCodigoIgnoreCaseAndEstadoTrue(String codigo);

    boolean existsByNombreIgnoreCaseAndEstadoTrue(String nombre);

    boolean existsByCodigoIgnoreCaseAndEstadoTrueAndIdAlmacenNot(String codigo, Long idAlmacen);

    boolean existsByNombreIgnoreCaseAndEstadoTrueAndIdAlmacenNot(String nombre, Long idAlmacen);

    boolean existsByPrincipalTrueAndEstadoTrue();

    boolean existsByPrincipalTrueAndEstadoTrueAndIdAlmacenNot(Long idAlmacen);

    Page<Almacen> findByEstadoTrue(Pageable pageable);

    List<Almacen> findByEstadoTrueOrderByPrincipalDescNombreAsc();

    List<Almacen> findByPermiteVentaTrueAndEstadoTrueOrderByPrincipalDescNombreAsc();

    List<Almacen> findByPermiteCompraTrueAndEstadoTrueOrderByPrincipalDescNombreAsc();
}