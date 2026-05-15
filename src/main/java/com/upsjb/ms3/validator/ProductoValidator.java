// ruta: src/main/java/com/upsjb/ms3/validator/ProductoValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import org.springframework.stereotype.Component;

@Component
public class ProductoValidator {

    public void validateCreate(
            TipoProducto tipoProducto,
            Categoria categoria,
            Marca marca,
            String codigoProducto,
            String nombre,
            Long creadoPorIdUsuarioMs1,
            boolean duplicatedCodigo,
            boolean duplicatedNombre,
            boolean duplicatedSlug
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateRequiredReferences(tipoProducto, categoria, marca, errors);
        validateCodigo(codigoProducto, errors);
        validateNombre(nombre, errors);

        if (creadoPorIdUsuarioMs1 == null) {
            errors.add("creadoPorIdUsuarioMs1", "El usuario creador es obligatorio.", "REQUIRED", null);
        }

        errors.throwIfAny("No se puede crear el producto.");

        requireNotDuplicated(duplicatedCodigo, "Ya existe un producto activo con el mismo código.");
        requireNotDuplicated(duplicatedNombre, "Ya existe un producto activo con el mismo nombre.");
        requireNotDuplicated(duplicatedSlug, "Ya existe un producto activo con el mismo slug.");
    }

    public void validateUpdate(
            Producto producto,
            TipoProducto tipoProducto,
            Categoria categoria,
            Marca marca,
            String nombre,
            Long actualizadoPorIdUsuarioMs1,
            boolean duplicatedNombre,
            boolean duplicatedSlug
    ) {
        requireEditable(producto);

        ValidationErrorCollector errors = ValidationErrorCollector.create();

        validateRequiredReferences(tipoProducto, categoria, marca, errors);
        validateNombre(nombre, errors);

        if (actualizadoPorIdUsuarioMs1 == null) {
            errors.add("actualizadoPorIdUsuarioMs1", "El usuario actualizador es obligatorio.", "REQUIRED", null);
        }

        errors.throwIfAny("No se puede actualizar el producto.");

        requireNotDuplicated(duplicatedNombre, "Ya existe otro producto activo con el mismo nombre.");
        requireNotDuplicated(duplicatedSlug, "Ya existe otro producto activo con el mismo slug.");
    }

    public void validateCanDeactivate(Producto producto) {
        requireActive(producto);

        if (producto.getEstadoPublicacion() != null && producto.getEstadoPublicacion().isPublicado()) {
            throw new ConflictException(
                    "PRODUCTO_PUBLICADO_NO_INACTIVABLE",
                    "No se puede inactivar un producto publicado. Primero debe ocultarse o despublicarse."
            );
        }
    }

    public void validateCanDiscontinue(Producto producto, boolean hasPendingReservations) {
        requireActive(producto);

        if (hasPendingReservations) {
            throw new ConflictException(
                    "PRODUCTO_CON_RESERVAS_PENDIENTES",
                    "No se puede descontinuar el producto porque tiene reservas pendientes."
            );
        }
    }

    public void validateVentaState(Producto producto, EstadoProductoVenta nuevoEstadoVenta) {
        requireActive(producto);

        if (nuevoEstadoVenta == null) {
            throw new ConflictException(
                    "ESTADO_VENTA_OBLIGATORIO",
                    "El estado de venta es obligatorio."
            );
        }

        if (producto.getEstadoRegistro() == EstadoProductoRegistro.DESCONTINUADO
                && nuevoEstadoVenta.isVendible()) {
            throw new ConflictException(
                    "PRODUCTO_DESCONTINUADO_NO_VENDIBLE",
                    "Un producto descontinuado no puede marcarse como vendible."
            );
        }
    }

    public void requireActive(Producto producto) {
        requireExists(producto);

        if (!producto.isActivo()) {
            throw new NotFoundException(
                    "PRODUCTO_INACTIVO",
                    "El producto no está activo."
            );
        }
    }

    public void requireEditable(Producto producto) {
        requireActive(producto);

        if (producto.getEstadoRegistro() != null && !producto.getEstadoRegistro().isEditable()) {
            throw new ConflictException(
                    "PRODUCTO_NO_EDITABLE",
                    "El producto no se puede editar en su estado actual."
            );
        }
    }

    private void requireExists(Producto producto) {
        if (producto == null) {
            throw new NotFoundException(
                    "PRODUCTO_NO_ENCONTRADO",
                    "Producto no encontrado."
            );
        }
    }

    private void validateRequiredReferences(
            TipoProducto tipoProducto,
            Categoria categoria,
            Marca marca,
            ValidationErrorCollector errors
    ) {
        if (tipoProducto == null) {
            errors.add("tipoProducto", "El tipo de producto es obligatorio.", "REQUIRED", null);
        } else if (!tipoProducto.isActivo()) {
            errors.add("tipoProducto", "El tipo de producto debe estar activo.", "INACTIVE", tipoProducto.getIdTipoProducto());
        }

        if (categoria == null) {
            errors.add("categoria", "La categoría es obligatoria.", "REQUIRED", null);
        } else if (!categoria.isActivo()) {
            errors.add("categoria", "La categoría debe estar activa.", "INACTIVE", categoria.getIdCategoria());
        }

        if (marca != null && !marca.isActivo()) {
            errors.add("marca", "La marca debe estar activa.", "INACTIVE", marca.getIdMarca());
        }
    }

    private void validateCodigo(String codigo, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(codigo)) {
            errors.add("codigoProducto", "El código del producto es obligatorio.", "REQUIRED", codigo);
            return;
        }

        if (StringNormalizer.clean(codigo).length() > 80) {
            errors.add("codigoProducto", "El código del producto no debe superar 80 caracteres.", "MAX_LENGTH", codigo);
        }
    }

    private void validateNombre(String nombre, ValidationErrorCollector errors) {
        if (!StringNormalizer.hasText(nombre)) {
            errors.add("nombre", "El nombre del producto es obligatorio.", "REQUIRED", nombre);
            return;
        }

        if (StringNormalizer.clean(nombre).length() > 180) {
            errors.add("nombre", "El nombre del producto no debe superar 180 caracteres.", "MAX_LENGTH", nombre);
        }
    }

    private void requireNotDuplicated(boolean duplicated, String message) {
        if (duplicated) {
            throw new ConflictException("PRODUCTO_DUPLICADO", message);
        }
    }
}