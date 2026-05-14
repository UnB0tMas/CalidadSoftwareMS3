// ruta: src/main/java/com/upsjb/ms3/mapper/ProductoMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoAtributoValorResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoImagenResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoPublicDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoPublicResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoSkuResponseDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductoMapper {

    private final ReferenceMapper referenceMapper;

    public Producto toEntity(
            ProductoCreateRequestDto request,
            TipoProducto tipoProducto,
            Categoria categoria,
            Marca marca,
            String codigoProducto,
            String slug,
            Long creadoPorIdUsuarioMs1
    ) {
        if (request == null) {
            return null;
        }

        Producto entity = new Producto();
        entity.setTipoProducto(tipoProducto);
        entity.setCategoria(categoria);
        entity.setMarca(marca);
        entity.setCodigoProducto(codigoProducto);
        entity.setCodigoGenerado(Boolean.TRUE);
        entity.setNombre(request.nombre());
        entity.setSlug(slug);
        entity.setSlugGenerado(Boolean.TRUE);
        entity.setDescripcionCorta(request.descripcionCorta());
        entity.setDescripcionLarga(request.descripcionLarga());
        entity.setGeneroObjetivo(request.generoObjetivo());
        entity.setTemporada(request.temporada());
        entity.setDeporte(request.deporte());
        entity.setEstadoRegistro(EstadoProductoRegistro.BORRADOR);
        entity.setEstadoPublicacion(EstadoProductoPublicacion.NO_PUBLICADO);
        entity.setEstadoVenta(EstadoProductoVenta.NO_VENDIBLE);
        entity.setVisiblePublico(Boolean.FALSE);
        entity.setVendible(Boolean.FALSE);
        entity.setCreadoPorIdUsuarioMs1(creadoPorIdUsuarioMs1);

        return entity;
    }

    public void updateEntity(
            Producto entity,
            ProductoUpdateRequestDto request,
            TipoProducto tipoProducto,
            Categoria categoria,
            Marca marca,
            String slug,
            Long actualizadoPorIdUsuarioMs1
    ) {
        if (entity == null || request == null) {
            return;
        }

        entity.setTipoProducto(tipoProducto);
        entity.setCategoria(categoria);
        entity.setMarca(marca);
        entity.setNombre(request.nombre());

        if (slug != null) {
            entity.setSlug(slug);
            entity.setSlugGenerado(Boolean.TRUE);
        }

        entity.setDescripcionCorta(request.descripcionCorta());
        entity.setDescripcionLarga(request.descripcionLarga());
        entity.setGeneroObjetivo(request.generoObjetivo());
        entity.setTemporada(request.temporada());
        entity.setDeporte(request.deporte());
        entity.setActualizadoPorIdUsuarioMs1(actualizadoPorIdUsuarioMs1);
    }

    public ProductoResponseDto toResponse(Producto entity) {
        if (entity == null) {
            return null;
        }

        return ProductoResponseDto.builder()
                .idProducto(entity.getIdProducto())
                .tipoProducto(referenceMapper.toIdCodigoNombre(entity.getTipoProducto()))
                .categoria(referenceMapper.toIdCodigoNombre(entity.getCategoria()))
                .marca(referenceMapper.toIdCodigoNombre(entity.getMarca()))
                .codigoProducto(entity.getCodigoProducto())
                .codigoGenerado(entity.getCodigoGenerado())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .slugGenerado(entity.getSlugGenerado())
                .descripcionCorta(entity.getDescripcionCorta())
                .descripcionLarga(entity.getDescripcionLarga())
                .generoObjetivo(entity.getGeneroObjetivo())
                .temporada(entity.getTemporada())
                .deporte(entity.getDeporte())
                .estadoRegistro(entity.getEstadoRegistro())
                .estadoPublicacion(entity.getEstadoPublicacion())
                .estadoVenta(entity.getEstadoVenta())
                .visiblePublico(entity.getVisiblePublico())
                .vendible(entity.getVendible())
                .fechaPublicacionInicio(entity.getFechaPublicacionInicio())
                .fechaPublicacionFin(entity.getFechaPublicacionFin())
                .motivoEstado(entity.getMotivoEstado())
                .creadoPorIdUsuarioMs1(entity.getCreadoPorIdUsuarioMs1())
                .actualizadoPorIdUsuarioMs1(entity.getActualizadoPorIdUsuarioMs1())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ProductoDetailResponseDto toDetailResponse(
            Producto entity,
            List<ProductoSkuResponseDto> skus,
            List<ProductoAtributoValorResponseDto> atributos,
            List<ProductoImagenResponseDto> imagenes
    ) {
        if (entity == null) {
            return null;
        }

        return ProductoDetailResponseDto.builder()
                .idProducto(entity.getIdProducto())
                .tipoProducto(referenceMapper.toIdCodigoNombre(entity.getTipoProducto()))
                .categoria(referenceMapper.toIdCodigoNombre(entity.getCategoria()))
                .marca(referenceMapper.toIdCodigoNombre(entity.getMarca()))
                .codigoProducto(entity.getCodigoProducto())
                .codigoGenerado(entity.getCodigoGenerado())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .slugGenerado(entity.getSlugGenerado())
                .descripcionCorta(entity.getDescripcionCorta())
                .descripcionLarga(entity.getDescripcionLarga())
                .generoObjetivo(entity.getGeneroObjetivo())
                .temporada(entity.getTemporada())
                .deporte(entity.getDeporte())
                .estadoRegistro(entity.getEstadoRegistro())
                .estadoPublicacion(entity.getEstadoPublicacion())
                .estadoVenta(entity.getEstadoVenta())
                .visiblePublico(entity.getVisiblePublico())
                .vendible(entity.getVendible())
                .fechaPublicacionInicio(entity.getFechaPublicacionInicio())
                .fechaPublicacionFin(entity.getFechaPublicacionFin())
                .motivoEstado(entity.getMotivoEstado())
                .creadoPorIdUsuarioMs1(entity.getCreadoPorIdUsuarioMs1())
                .actualizadoPorIdUsuarioMs1(entity.getActualizadoPorIdUsuarioMs1())
                .estado(entity.getEstado())
                .skus(emptyList(skus))
                .atributos(emptyList(atributos))
                .imagenes(emptyList(imagenes))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ProductoPublicResponseDto toPublicResponse(
            Producto entity,
            String imagenPrincipalUrl,
            MoneyResponseDto precioDesde,
            MoneyResponseDto precioFinalDesde,
            Boolean tienePromocion,
            List<String> coloresDisponibles,
            List<String> tallasDisponibles
    ) {
        if (entity == null) {
            return null;
        }

        Categoria categoria = entity.getCategoria();
        Marca marca = entity.getMarca();

        return ProductoPublicResponseDto.builder()
                .idProducto(entity.getIdProducto())
                .codigoProducto(entity.getCodigoProducto())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .descripcionCorta(entity.getDescripcionCorta())
                .categoriaNombre(categoria == null ? null : categoria.getNombre())
                .categoriaSlug(categoria == null ? null : categoria.getSlug())
                .marcaNombre(marca == null ? null : marca.getNombre())
                .imagenPrincipalUrl(imagenPrincipalUrl)
                .precioDesde(precioDesde)
                .precioFinalDesde(precioFinalDesde)
                .tienePromocion(defaultBoolean(tienePromocion, false))
                .estadoVenta(entity.getEstadoVenta())
                .vendible(entity.getVendible())
                .coloresDisponibles(emptyList(coloresDisponibles))
                .tallasDisponibles(emptyList(tallasDisponibles))
                .build();
    }

    public ProductoPublicDetailResponseDto toPublicDetailResponse(
            Producto entity,
            MoneyResponseDto precioDesde,
            MoneyResponseDto precioFinalDesde,
            Boolean tienePromocion,
            List<ProductoSkuResponseDto> skus,
            List<ProductoAtributoValorResponseDto> atributos,
            List<ProductoImagenResponseDto> imagenes
    ) {
        if (entity == null) {
            return null;
        }

        Categoria categoria = entity.getCategoria();
        Marca marca = entity.getMarca();

        return ProductoPublicDetailResponseDto.builder()
                .idProducto(entity.getIdProducto())
                .codigoProducto(entity.getCodigoProducto())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .descripcionCorta(entity.getDescripcionCorta())
                .descripcionLarga(entity.getDescripcionLarga())
                .categoriaNombre(categoria == null ? null : categoria.getNombre())
                .categoriaSlug(categoria == null ? null : categoria.getSlug())
                .marcaNombre(marca == null ? null : marca.getNombre())
                .generoObjetivo(entity.getGeneroObjetivo() == null ? null : entity.getGeneroObjetivo().getLabel())
                .temporada(entity.getTemporada())
                .deporte(entity.getDeporte())
                .estadoVenta(entity.getEstadoVenta())
                .vendible(entity.getVendible())
                .precioDesde(precioDesde)
                .precioFinalDesde(precioFinalDesde)
                .tienePromocion(defaultBoolean(tienePromocion, false))
                .skus(emptyList(skus))
                .atributos(emptyList(atributos))
                .imagenes(emptyList(imagenes))
                .build();
    }

    public void applyEstadoRegistro(
            Producto entity,
            EstadoProductoRegistro estadoRegistro,
            String motivo,
            Long actualizadoPorIdUsuarioMs1
    ) {
        if (entity == null) {
            return;
        }

        entity.setEstadoRegistro(estadoRegistro);
        entity.setMotivoEstado(motivo);
        entity.setActualizadoPorIdUsuarioMs1(actualizadoPorIdUsuarioMs1);
    }

    public void applyPublicacion(
            Producto entity,
            EstadoProductoPublicacion estadoPublicacion,
            Boolean visiblePublico,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            String motivo,
            Long actualizadoPorIdUsuarioMs1
    ) {
        if (entity == null) {
            return;
        }

        entity.setEstadoPublicacion(estadoPublicacion);
        entity.setVisiblePublico(defaultBoolean(visiblePublico, Boolean.TRUE.equals(entity.getVisiblePublico())));
        entity.setFechaPublicacionInicio(fechaInicio);
        entity.setFechaPublicacionFin(fechaFin);
        entity.setMotivoEstado(motivo);
        entity.setActualizadoPorIdUsuarioMs1(actualizadoPorIdUsuarioMs1);
    }

    public void applyVenta(
            Producto entity,
            EstadoProductoVenta estadoVenta,
            Boolean vendible,
            String motivo,
            Long actualizadoPorIdUsuarioMs1
    ) {
        if (entity == null) {
            return;
        }

        entity.setEstadoVenta(estadoVenta);
        entity.setVendible(defaultBoolean(vendible, estadoVenta != null && estadoVenta.isVendible()));
        entity.setMotivoEstado(motivo);
        entity.setActualizadoPorIdUsuarioMs1(actualizadoPorIdUsuarioMs1);
    }

    private <T> List<T> emptyList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private Boolean defaultBoolean(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }
}