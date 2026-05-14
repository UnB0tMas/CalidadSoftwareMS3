// ruta: src/main/java/com/upsjb/ms3/repository/StockSkuRepository.java
package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.StockSku;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockSkuRepository extends
        JpaRepository<StockSku, Long>,
        JpaSpecificationExecutor<StockSku> {

    Optional<StockSku> findByIdStockAndEstadoTrue(Long idStock);

    Optional<StockSku> findBySku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrue(Long idSku, Long idAlmacen);

    boolean existsBySku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrue(Long idSku, Long idAlmacen);

    boolean existsBySku_IdSkuAndEstadoTrue(Long idSku);

    boolean existsByAlmacen_IdAlmacenAndEstadoTrue(Long idAlmacen);

    boolean existsBySku_IdSkuAndEstadoTrueAndStockDisponibleGreaterThanEqual(Long idSku, Integer cantidad);

    boolean existsBySku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrueAndStockDisponibleGreaterThanEqual(
            Long idSku,
            Long idAlmacen,
            Integer cantidad
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from StockSku s
            where s.idStock = :idStock
              and s.estado = true
            """)
    Optional<StockSku> findActivoByIdForUpdate(@Param("idStock") Long idStock);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from StockSku s
            where s.sku.idSku = :idSku
              and s.almacen.idAlmacen = :idAlmacen
              and s.estado = true
            """)
    Optional<StockSku> findActivoBySkuAndAlmacenForUpdate(
            @Param("idSku") Long idSku,
            @Param("idAlmacen") Long idAlmacen
    );

    Page<StockSku> findByEstadoTrue(Pageable pageable);

    Page<StockSku> findBySku_IdSkuAndEstadoTrue(Long idSku, Pageable pageable);

    Page<StockSku> findByAlmacen_IdAlmacenAndEstadoTrue(Long idAlmacen, Pageable pageable);

    List<StockSku> findBySku_IdSkuAndEstadoTrueOrderByAlmacen_PrincipalDescAlmacen_NombreAsc(Long idSku);

    List<StockSku> findByAlmacen_IdAlmacenAndEstadoTrueOrderBySku_CodigoSkuAsc(Long idAlmacen);

    List<StockSku> findByEstadoTrueAndStockDisponibleGreaterThanOrderBySku_CodigoSkuAsc(Integer stockDisponible);

    List<StockSku> findByEstadoTrueAndStockDisponibleLessThanEqualOrderByStockDisponibleAsc(Integer stockDisponible);

    List<StockSku> findByEstadoTrueAndStockFisicoLessThanEqualOrderByStockFisicoAsc(Integer stockFisico);

    @Query("""
            select coalesce(sum(s.stockFisico), 0)
            from StockSku s
            where s.sku.idSku = :idSku
              and s.estado = true
            """)
    Long sumStockFisicoBySku(@Param("idSku") Long idSku);

    @Query("""
            select coalesce(sum(s.stockReservado), 0)
            from StockSku s
            where s.sku.idSku = :idSku
              and s.estado = true
            """)
    Long sumStockReservadoBySku(@Param("idSku") Long idSku);

    @Query("""
            select coalesce(sum(s.stockDisponible), 0)
            from StockSku s
            where s.sku.idSku = :idSku
              and s.estado = true
            """)
    Long sumStockDisponibleBySku(@Param("idSku") Long idSku);

    @Query("""
            select coalesce(sum(s.stockDisponible), 0)
            from StockSku s
            where s.sku.producto.idProducto = :idProducto
              and s.estado = true
            """)
    Long sumStockDisponibleByProducto(@Param("idProducto") Long idProducto);

    @Query("""
            select count(distinct s.sku.idSku)
            from StockSku s
            where s.almacen.idAlmacen = :idAlmacen
              and s.estado = true
            """)
    Long countDistinctSkuByAlmacen(@Param("idAlmacen") Long idAlmacen);

    @Query("""
            select coalesce(sum(s.stockFisico), 0)
            from StockSku s
            where s.almacen.idAlmacen = :idAlmacen
              and s.estado = true
            """)
    Long sumStockFisicoByAlmacen(@Param("idAlmacen") Long idAlmacen);

    @Query("""
            select coalesce(sum(s.stockReservado), 0)
            from StockSku s
            where s.almacen.idAlmacen = :idAlmacen
              and s.estado = true
            """)
    Long sumStockReservadoByAlmacen(@Param("idAlmacen") Long idAlmacen);

    @Query("""
            select coalesce(sum(s.stockDisponible), 0)
            from StockSku s
            where s.almacen.idAlmacen = :idAlmacen
              and s.estado = true
            """)
    Long sumStockDisponibleByAlmacen(@Param("idAlmacen") Long idAlmacen);
}