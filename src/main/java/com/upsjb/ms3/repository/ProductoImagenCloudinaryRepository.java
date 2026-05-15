// ruta: src/main/java/com/upsjb/ms3/repository/ProductoImagenCloudinaryRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.ProductoImagenCloudinary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductoImagenCloudinaryRepository extends
        JpaRepository<ProductoImagenCloudinary, Long>,
        JpaSpecificationExecutor<ProductoImagenCloudinary> {

    @EntityGraph(attributePaths = {
            "producto",
            "producto.categoria",
            "producto.marca",
            "sku",
            "sku.producto"
    })
    @Override
    Page<ProductoImagenCloudinary> findAll(
            Specification<ProductoImagenCloudinary> specification,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "producto",
            "producto.categoria",
            "producto.marca",
            "sku",
            "sku.producto"
    })
    Optional<ProductoImagenCloudinary> findByIdImagenAndEstadoTrue(Long idImagen);

    Optional<ProductoImagenCloudinary> findByCloudinaryPublicIdAndEstadoTrue(String cloudinaryPublicId);

    Optional<ProductoImagenCloudinary> findByCloudinaryAssetIdAndEstadoTrue(String cloudinaryAssetId);

    boolean existsByCloudinaryPublicIdAndEstadoTrue(String cloudinaryPublicId);

    boolean existsByCloudinaryAssetIdAndEstadoTrue(String cloudinaryAssetId);

    @EntityGraph(attributePaths = {
            "producto",
            "sku"
    })
    Page<ProductoImagenCloudinary> findByProducto_IdProductoAndEstadoTrue(Long idProducto, Pageable pageable);

    @EntityGraph(attributePaths = {
            "producto",
            "sku"
    })
    Page<ProductoImagenCloudinary> findBySku_IdSkuAndEstadoTrue(Long idSku, Pageable pageable);

    @EntityGraph(attributePaths = {
            "producto",
            "sku"
    })
    List<ProductoImagenCloudinary> findByProducto_IdProductoAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(
            Long idProducto
    );

    @EntityGraph(attributePaths = {
            "producto",
            "sku"
    })
    List<ProductoImagenCloudinary> findByProducto_IdProductoAndSkuIsNullAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(
            Long idProducto
    );

    @EntityGraph(attributePaths = {
            "producto",
            "sku"
    })
    List<ProductoImagenCloudinary> findBySku_IdSkuAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(Long idSku);

    @EntityGraph(attributePaths = {
            "producto",
            "sku"
    })
    Optional<ProductoImagenCloudinary> findFirstByProducto_IdProductoAndSkuIsNullAndPrincipalTrueAndEstadoTrueOrderByOrdenAscIdImagenAsc(
            Long idProducto
    );

    @EntityGraph(attributePaths = {
            "producto",
            "sku"
    })
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