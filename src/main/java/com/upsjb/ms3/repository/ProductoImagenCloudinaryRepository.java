// ruta: src/main/java/com/upsjb/ms3/repository/ProductoImagenCloudinaryRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.ProductoImagenCloudinary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductoImagenCloudinaryRepository extends
        JpaRepository<ProductoImagenCloudinary, Long>,
        JpaSpecificationExecutor<ProductoImagenCloudinary> {

    Optional<ProductoImagenCloudinary> findByIdImagenAndEstadoTrue(Long idImagen);

    Optional<ProductoImagenCloudinary> findByCloudinaryPublicIdAndEstadoTrue(String cloudinaryPublicId);

    Optional<ProductoImagenCloudinary> findByCloudinaryAssetIdAndEstadoTrue(String cloudinaryAssetId);

    boolean existsByCloudinaryPublicIdAndEstadoTrue(String cloudinaryPublicId);

    boolean existsByCloudinaryAssetIdAndEstadoTrue(String cloudinaryAssetId);

    Page<ProductoImagenCloudinary> findByProducto_IdProductoAndEstadoTrue(Long idProducto, Pageable pageable);

    Page<ProductoImagenCloudinary> findBySku_IdSkuAndEstadoTrue(Long idSku, Pageable pageable);

    List<ProductoImagenCloudinary> findByProducto_IdProductoAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(
            Long idProducto
    );

    List<ProductoImagenCloudinary> findByProducto_IdProductoAndSkuIsNullAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(
            Long idProducto
    );

    List<ProductoImagenCloudinary> findBySku_IdSkuAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(Long idSku);

    Optional<ProductoImagenCloudinary> findFirstByProducto_IdProductoAndSkuIsNullAndPrincipalTrueAndEstadoTrueOrderByOrdenAscIdImagenAsc(
            Long idProducto
    );

    Optional<ProductoImagenCloudinary> findFirstBySku_IdSkuAndPrincipalTrueAndEstadoTrueOrderByOrdenAscIdImagenAsc(
            Long idSku
    );

    boolean existsByProducto_IdProductoAndSkuIsNullAndPrincipalTrueAndEstadoTrue(Long idProducto);

    boolean existsBySku_IdSkuAndPrincipalTrueAndEstadoTrue(Long idSku);

    boolean existsByProducto_IdProductoAndSkuIsNullAndPrincipalTrueAndEstadoTrueAndIdImagenNot(
            Long idProducto,
            Long idImagen
    );

    boolean existsBySku_IdSkuAndPrincipalTrueAndEstadoTrueAndIdImagenNot(Long idSku, Long idImagen);

    long countByProducto_IdProductoAndEstadoTrue(Long idProducto);

    long countBySku_IdSkuAndEstadoTrue(Long idSku);
}