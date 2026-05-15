// ruta: src/main/java/com/upsjb/ms3/validator/ProductoPublicacionValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.config.AppPropertiesConfig;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductoPublicacionValidator {

    private final AppPropertiesConfig appProperties;

    public void validateCanPublish(
            Producto producto,
            boolean hasActiveSku,
            boolean hasCurrentPrice,
            boolean hasMainImage,
            boolean hasAvailableStock
    ) {
        requireActive(producto);

        if (producto.getEstadoRegistro() != EstadoProductoRegistro.ACTIVO) {
            throw new ConflictException(
                    "PRODUCTO_NO_PUBLICABLE",
                    "Solo se puede publicar un producto con estado de registro ACTIVO."
            );
        }

        if (appProperties.getCatalog().isRequireActiveSkuToPublish() && !hasActiveSku) {
            throw new ConflictException(
                    "PRODUCTO_NO_TIENE_SKU_ACTIVO",
                    "No se puede publicar el producto porque no tiene SKU activo."
            );
        }

        if (appProperties.getCatalog().isRequireCurrentPriceToPublish() && !hasCurrentPrice) {
            throw new ConflictException(
                    "PRODUCTO_NO_TIENE_PRECIO_VIGENTE",
                    "No se puede publicar el producto porque no tiene precio vigente."
            );
        }

        if (appProperties.getCatalog().isRequireMainImageToPublish() && !hasMainImage) {
            throw new ConflictException(
                    "PRODUCTO_NO_TIENE_IMAGEN_PRINCIPAL",
                    "No se puede publicar el producto porque no tiene imagen principal activa."
            );
        }

        if (producto.getEstadoVenta() == EstadoProductoVenta.VENDIBLE && !hasAvailableStock) {
            throw new ConflictException(
                    "PRODUCTO_NO_TIENE_STOCK_DISPONIBLE",
                    "No se puede marcar como vendible porque no tiene stock disponible."
            );
        }
    }

    public void validatePublishNow(Producto producto) {
        requireActive(producto);

        if (producto.getEstadoPublicacion() == EstadoProductoPublicacion.PUBLICADO) {
            throw new ConflictException(
                    "PRODUCTO_YA_PUBLICADO",
                    "El producto ya se encuentra publicado."
            );
        }
    }

    public void validateSchedulePublication(
            Producto producto,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    ) {
        requireActive(producto);

        if (fechaInicio == null) {
            throw new ConflictException(
                    "FECHA_PUBLICACION_INICIO_OBLIGATORIA",
                    "La fecha de inicio de publicación es obligatoria."
            );
        }

        if (fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new ConflictException(
                    "RANGO_PUBLICACION_INVALIDO",
                    "La fecha fin de publicación no puede ser menor que la fecha inicio."
            );
        }
    }

    public void validateCanHide(Producto producto) {
        requireActive(producto);

        if (producto.getEstadoPublicacion() == EstadoProductoPublicacion.OCULTO) {
            throw new ConflictException(
                    "PRODUCTO_YA_OCULTO",
                    "El producto ya se encuentra oculto."
            );
        }
    }

    public void validateCoherentPublicationAndSaleState(
            EstadoProductoPublicacion estadoPublicacion,
            EstadoProductoVenta estadoVenta
    ) {
        if (estadoPublicacion == null || estadoVenta == null) {
            throw new ConflictException(
                    "ESTADOS_PRODUCTO_OBLIGATORIOS",
                    "El estado de publicación y el estado de venta son obligatorios."
            );
        }

        if (estadoPublicacion == EstadoProductoPublicacion.NO_PUBLICADO && estadoVenta.isVendible()) {
            throw new ConflictException(
                    "PRODUCTO_NO_PUBLICADO_NO_VENDIBLE",
                    "Un producto no publicado no puede estar en estado VENDIBLE."
            );
        }

        if (estadoPublicacion == EstadoProductoPublicacion.PROGRAMADO
                && estadoVenta != EstadoProductoVenta.PROXIMAMENTE) {
            throw new ConflictException(
                    "PRODUCTO_PROGRAMADO_ESTADO_VENTA_INVALIDO",
                    "Un producto programado debe tener estado de venta PROXIMAMENTE."
            );
        }
    }

    private void requireActive(Producto producto) {
        if (producto == null) {
            throw new NotFoundException(
                    "PRODUCTO_NO_ENCONTRADO",
                    "Producto no encontrado."
            );
        }

        if (!producto.isActivo()) {
            throw new NotFoundException(
                    "PRODUCTO_INACTIVO",
                    "El producto no está activo."
            );
        }
    }
}