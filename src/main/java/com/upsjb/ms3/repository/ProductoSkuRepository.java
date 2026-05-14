// ruta: src/main/java/com/upsjb/ms3/repository/ProductoSkuRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.enums.EstadoSku;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductoSkuRepository extends
        JpaRepository<ProductoSku, Long>,
        JpaSpecificationExecutor<ProductoSku> {

    Optional<ProductoSku> findByIdSkuAndEstadoTrue(Long idSku);

    Optional<ProductoSku> findByCodigoSkuIgnoreCaseAndEstadoTrue(String codigoSku);

    Optional<ProductoSku> findByBarcodeIgnoreCaseAndEstadoTrue(String barcode);

    boolean existsByCodigoSkuIgnoreCaseAndEstadoTrue(String codigoSku);

    boolean existsByBarcodeIgnoreCaseAndEstadoTrue(String barcode);

    boolean existsByCodigoSkuIgnoreCaseAndEstadoTrueAndIdSkuNot(String codigoSku, Long idSku);

    boolean existsByBarcodeIgnoreCaseAndEstadoTrueAndIdSkuNot(String barcode, Long idSku);

    boolean existsByProducto_IdProductoAndEstadoTrue(Long idProducto);

    boolean existsByProducto_IdProductoAndEstadoTrueAndEstadoSku(Long idProducto, EstadoSku estadoSku);

    long countByProducto_IdProductoAndEstadoTrue(Long idProducto);

    long countByProducto_IdProductoAndEstadoTrueAndEstadoSku(Long idProducto, EstadoSku estadoSku);

    Page<ProductoSku> findByEstadoTrue(Pageable pageable);

    Page<ProductoSku> findByProducto_IdProductoAndEstadoTrue(Long idProducto, Pageable pageable);

    List<ProductoSku> findByProducto_IdProductoAndEstadoTrueOrderByIdSkuAsc(Long idProducto);

    List<ProductoSku> findByProducto_IdProductoAndEstadoTrueAndEstadoSkuOrderByIdSkuAsc(
            Long idProducto,
            EstadoSku estadoSku
    );

    List<ProductoSku> findByEstadoTrueAndEstadoSkuOrderByIdSkuAsc(EstadoSku estadoSku);
}