// ruta: src/main/java/com/upsjb/ms3/mapper/ProductoSkuMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.enums.EstadoSku;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoSkuCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoSkuUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoImagenResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoSkuDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoSkuResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.SkuAtributoValorResponseDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import com.upsjb.ms3.dto.shared.StockResumenResponseDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProductoSkuMapper {

    public ProductoSku toEntity(
            ProductoSkuCreateRequestDto request,
            Producto producto,
            String codigoSku
    ) {
        if (request == null) {
            return null;
        }

        ProductoSku entity = new ProductoSku();
        entity.setProducto(producto);
        entity.setCodigoSku(codigoSku);
        entity.setCodigoGenerado(Boolean.TRUE);
        entity.setBarcode(request.barcode());
        entity.setColor(request.color());
        entity.setTalla(request.talla());
        entity.setMaterial(request.material());
        entity.setModelo(request.modelo());
        entity.setStockMinimo(defaultInteger(request.stockMinimo(), 0));
        entity.setStockMaximo(request.stockMaximo());
        entity.setPesoGramos(request.pesoGramos());
        entity.setAltoCm(request.altoCm());
        entity.setAnchoCm(request.anchoCm());
        entity.setLargoCm(request.largoCm());
        entity.setEstadoSku(EstadoSku.ACTIVO);

        return entity;
    }

    public void updateEntity(ProductoSku entity, ProductoSkuUpdateRequestDto request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setBarcode(request.barcode());
        entity.setColor(request.color());
        entity.setTalla(request.talla());
        entity.setMaterial(request.material());
        entity.setModelo(request.modelo());
        entity.setStockMinimo(defaultInteger(request.stockMinimo(), 0));
        entity.setStockMaximo(request.stockMaximo());
        entity.setPesoGramos(request.pesoGramos());
        entity.setAltoCm(request.altoCm());
        entity.setAnchoCm(request.anchoCm());
        entity.setLargoCm(request.largoCm());

        if (request.estadoSku() != null) {
            entity.setEstadoSku(request.estadoSku());
        }
    }

    public ProductoSkuResponseDto toResponse(
            ProductoSku entity,
            MoneyResponseDto precioVigente,
            MoneyResponseDto precioFinalPromocion,
            StockResumenResponseDto stockResumen
    ) {
        if (entity == null) {
            return null;
        }

        Producto producto = entity.getProducto();

        return ProductoSkuResponseDto.builder()
                .idSku(entity.getIdSku())
                .idProducto(producto == null ? null : producto.getIdProducto())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .codigoSku(entity.getCodigoSku())
                .codigoGenerado(entity.getCodigoGenerado())
                .barcode(entity.getBarcode())
                .color(entity.getColor())
                .talla(entity.getTalla())
                .material(entity.getMaterial())
                .modelo(entity.getModelo())
                .stockMinimo(entity.getStockMinimo())
                .stockMaximo(entity.getStockMaximo())
                .pesoGramos(entity.getPesoGramos())
                .altoCm(entity.getAltoCm())
                .anchoCm(entity.getAnchoCm())
                .largoCm(entity.getLargoCm())
                .estadoSku(entity.getEstadoSku())
                .estado(entity.getEstado())
                .precioVigente(precioVigente)
                .precioFinalPromocion(precioFinalPromocion)
                .stockResumen(stockResumen)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ProductoSkuDetailResponseDto toDetailResponse(
            ProductoSku entity,
            MoneyResponseDto precioVigente,
            MoneyResponseDto precioFinalPromocion,
            List<StockResumenResponseDto> stocks,
            List<SkuAtributoValorResponseDto> atributos,
            List<ProductoImagenResponseDto> imagenes
    ) {
        if (entity == null) {
            return null;
        }

        Producto producto = entity.getProducto();

        return ProductoSkuDetailResponseDto.builder()
                .idSku(entity.getIdSku())
                .idProducto(producto == null ? null : producto.getIdProducto())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .codigoSku(entity.getCodigoSku())
                .codigoGenerado(entity.getCodigoGenerado())
                .barcode(entity.getBarcode())
                .color(entity.getColor())
                .talla(entity.getTalla())
                .material(entity.getMaterial())
                .modelo(entity.getModelo())
                .stockMinimo(entity.getStockMinimo())
                .stockMaximo(entity.getStockMaximo())
                .pesoGramos(entity.getPesoGramos())
                .altoCm(entity.getAltoCm())
                .anchoCm(entity.getAnchoCm())
                .largoCm(entity.getLargoCm())
                .estadoSku(entity.getEstadoSku())
                .estado(entity.getEstado())
                .precioVigente(precioVigente)
                .precioFinalPromocion(precioFinalPromocion)
                .stocks(stocks == null ? List.of() : stocks)
                .atributos(atributos == null ? List.of() : atributos)
                .imagenes(imagenes == null ? List.of() : imagenes)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Integer defaultInteger(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }
}