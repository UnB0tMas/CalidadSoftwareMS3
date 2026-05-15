// ruta: src/main/java/com/upsjb/ms3/repository/ProveedorRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.Proveedor;
import com.upsjb.ms3.domain.enums.TipoDocumentoProveedor;
import com.upsjb.ms3.domain.enums.TipoProveedor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProveedorRepository extends
        JpaRepository<Proveedor, Long>,
        JpaSpecificationExecutor<Proveedor> {

    @Override
    Page<Proveedor> findAll(Specification<Proveedor> specification, Pageable pageable);

    Optional<Proveedor> findByIdProveedorAndEstadoTrue(Long idProveedor);

    Optional<Proveedor> findByRucAndEstadoTrue(String ruc);

    Optional<Proveedor> findByNumeroDocumentoAndEstadoTrue(String numeroDocumento);

    Optional<Proveedor> findByTipoDocumentoAndNumeroDocumentoAndEstadoTrue(
            TipoDocumentoProveedor tipoDocumento,
            String numeroDocumento
    );

    boolean existsByRucAndEstadoTrue(String ruc);

    boolean existsByNumeroDocumentoAndEstadoTrue(String numeroDocumento);

    boolean existsByTipoDocumentoAndNumeroDocumentoAndEstadoTrue(
            TipoDocumentoProveedor tipoDocumento,
            String numeroDocumento
    );

    boolean existsByRucAndEstadoTrueAndIdProveedorNot(String ruc, Long idProveedor);

    boolean existsByNumeroDocumentoAndEstadoTrueAndIdProveedorNot(String numeroDocumento, Long idProveedor);

    boolean existsByTipoDocumentoAndNumeroDocumentoAndEstadoTrueAndIdProveedorNot(
            TipoDocumentoProveedor tipoDocumento,
            String numeroDocumento,
            Long idProveedor
    );

    Page<Proveedor> findByEstadoTrue(Pageable pageable);

    Page<Proveedor> findByTipoProveedorAndEstadoTrue(TipoProveedor tipoProveedor, Pageable pageable);

    List<Proveedor> findByEstadoTrueOrderByRazonSocialAscNombreComercialAscNombresAscApellidosAsc();

    List<Proveedor> findByTipoProveedorAndEstadoTrueOrderByRazonSocialAscNombreComercialAscNombresAscApellidosAsc(
            TipoProveedor tipoProveedor
    );
}