package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.value.SlugValue;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import org.springframework.stereotype.Component;

@Component
public class CategoriaValidator {

    public void validateCreate(
            String codigo,
            String nombre,
            String slug,
            Categoria categoriaPadre,
            Integer orden,
            Boolean permiteProductos,
            boolean duplicatedCodigo,
            boolean duplicatedSiblingName,
            boolean duplicatedSlug
    ) {
        ValidationErrorCollector errors =
                ValidationErrorCollector.create();

        validateCodigo(codigo, errors);
        validateNombre(nombre, errors);
        validateSlug(slug, errors);
        validateOrden(orden, errors);
        validatePermiteProductos(
                permiteProductos,
                errors
        );
        validateParent(
                categoriaPadre,
                errors
        );

        errors.throwIfAny(
                "No se puede crear la categoría."
        );

        validateDuplicates(
                duplicatedCodigo,
                duplicatedSiblingName,
                duplicatedSlug
        );
    }

    public void validateUpdate(
            Categoria categoria,
            String nombre,
            Categoria categoriaPadre,
            Integer orden,
            Boolean permiteProductos,
            boolean duplicatedSiblingName,
            boolean wouldCreateCycle,
            boolean hasActiveProducts,
            boolean hasActiveChildren
    ) {
        requireActive(categoria);

        ValidationErrorCollector errors =
                ValidationErrorCollector.create();

        validateCodigo(
                categoria.getCodigo(),
                errors
        );
        validateNombre(nombre, errors);
        validateSlug(
                categoria.getSlug(),
                errors
        );
        validateOrden(orden, errors);
        validatePermiteProductos(
                permiteProductos,
                errors
        );
        validateParent(
                categoriaPadre,
                errors
        );

        if (wouldCreateCycle) {
            errors.add(
                    "categoriaPadre",
                    "La categoría padre seleccionada genera una jerarquía circular.",
                    "CYCLE_DETECTED",
                    categoriaPadre == null
                            ? null
                            : categoriaPadre.getIdCategoria()
            );
        }

        if (
                hasActiveProducts
                        && !Boolean.TRUE.equals(
                        permiteProductos
                )
        ) {
            errors.add(
                    "permiteProductos",
                    "La categoría tiene productos activos y debe continuar habilitada para recibir productos.",
                    "CATEGORY_HAS_PRODUCTS",
                    categoria.getIdCategoria()
            );
        }

        if (
                hasActiveChildren
                        && Boolean.TRUE.equals(
                        permiteProductos
                )
        ) {
            errors.add(
                    "permiteProductos",
                    "Una categoría con subcategorías activas solo puede organizar el catálogo y no puede recibir productos directamente.",
                    "CATEGORY_HAS_ACTIVE_CHILDREN",
                    categoria.getIdCategoria()
            );
        }

        errors.throwIfAny(
                "No se puede actualizar la categoría."
        );

        if (duplicatedSiblingName) {
            throw new ConflictException(
                    "CATEGORIA_NOMBRE_DUPLICADO_EN_PADRE",
                    "Ya existe una categoría activa con el mismo nombre dentro de la categoría padre seleccionada."
            );
        }
    }

    public void validateCanDeactivate(
            Categoria categoria,
            boolean hasActiveChildren,
            boolean hasActiveProducts
    ) {
        requireActive(categoria);

        if (hasActiveChildren) {
            throw new ConflictException(
                    "CATEGORIA_CON_SUBCATEGORIAS_ACTIVAS",
                    "No se puede inactivar la categoría porque tiene subcategorías activas."
            );
        }

        if (hasActiveProducts) {
            throw new ConflictException(
                    "CATEGORIA_CON_PRODUCTOS_ACTIVOS",
                    "No se puede inactivar la categoría porque tiene productos activos asociados."
            );
        }
    }

    public void validateCanActivate(
            Categoria categoria,
            boolean parentActive,
            boolean duplicatedSiblingName
    ) {
        requireExists(categoria);

        if (categoria.isActivo()) {
            throw new ConflictException(
                    "CATEGORIA_YA_ACTIVA",
                    "La categoría ya se encuentra activa."
            );
        }

        if (!parentActive) {
            throw new ConflictException(
                    "CATEGORIA_PADRE_INACTIVA",
                    "No se puede activar la categoría porque su categoría padre está inactiva."
            );
        }

        if (
                categoria.getCategoriaPadre() != null
                        && categoria.getCategoriaPadre()
                        .aceptaProductos()
        ) {
            throw new ConflictException(
                    "CATEGORIA_PADRE_NO_ADMITE_HIJOS",
                    "No se puede activar la categoría porque su categoría padre es una categoría final."
            );
        }

        if (duplicatedSiblingName) {
            throw new ConflictException(
                    "CATEGORIA_NOMBRE_DUPLICADO_EN_PADRE",
                    "Ya existe una categoría activa con el mismo nombre dentro de la categoría padre seleccionada."
            );
        }
    }

    public void requireSelectableForProduct(
            Categoria categoria
    ) {
        requireActive(categoria);

        if (!categoria.aceptaProductos()) {
            throw new ConflictException(
                    "CATEGORIA_NO_PERMITE_PRODUCTOS",
                    "La categoría seleccionada solo organiza el catálogo y no admite productos."
            );
        }
    }

    public void requireActive(
            Categoria categoria
    ) {
        requireExists(categoria);

        if (!categoria.isActivo()) {
            throw new ConflictException(
                    "CATEGORIA_INACTIVA",
                    "No se puede completar la operación porque la categoría está inactiva."
            );
        }
    }

    public void requireExists(
            Categoria categoria
    ) {
        if (categoria == null) {
            throw new NotFoundException(
                    "CATEGORIA_NO_ENCONTRADA",
                    "No se encontró la categoría solicitada."
            );
        }
    }

    private void validateCodigo(
            String codigo,
            ValidationErrorCollector errors
    ) {
        if (!StringNormalizer.hasText(codigo)) {
            errors.add(
                    "codigo",
                    "No se pudo generar el código de la categoría.",
                    "REQUIRED",
                    codigo
            );
            return;
        }

        if (codigo.length() > 50) {
            errors.add(
                    "codigo",
                    "El código generado no debe superar 50 caracteres.",
                    "MAX_LENGTH",
                    codigo
            );
        }
    }

    private void validateNombre(
            String nombre,
            ValidationErrorCollector errors
    ) {
        if (!StringNormalizer.hasText(nombre)) {
            errors.add(
                    "nombre",
                    "El nombre de la categoría es obligatorio.",
                    "REQUIRED",
                    nombre
            );
            return;
        }

        if (nombre.length() > 150) {
            errors.add(
                    "nombre",
                    "El nombre no debe superar 150 caracteres.",
                    "MAX_LENGTH",
                    nombre
            );
        }
    }

    private void validateSlug(
            String slug,
            ValidationErrorCollector errors
    ) {
        if (!StringNormalizer.hasText(slug)) {
            errors.add(
                    "slug",
                    "No se pudo generar el slug de la categoría.",
                    "REQUIRED",
                    slug
            );
            return;
        }

        if (slug.length() > SlugValue.MAX_LENGTH) {
            errors.add(
                    "slug",
                    "El slug no debe superar "
                            + SlugValue.MAX_LENGTH
                            + " caracteres.",
                    "MAX_LENGTH",
                    slug
            );
        }
    }

    private void validateOrden(
            Integer orden,
            ValidationErrorCollector errors
    ) {
        if (
                orden != null
                        && orden < 0
        ) {
            errors.add(
                    "orden",
                    "El orden no puede ser negativo.",
                    "MIN_VALUE",
                    orden
            );
        }
    }

    private void validatePermiteProductos(
            Boolean permiteProductos,
            ValidationErrorCollector errors
    ) {
        if (permiteProductos == null) {
            errors.add(
                    "permiteProductos",
                    "Debe indicar si la categoría admite productos.",
                    "REQUIRED",
                    null
            );
        }
    }

    private void validateParent(
            Categoria categoriaPadre,
            ValidationErrorCollector errors
    ) {
        if (categoriaPadre == null) {
            return;
        }

        if (!categoriaPadre.isActivo()) {
            errors.add(
                    "categoriaPadre",
                    "La categoría padre debe estar activa.",
                    "INACTIVE_REFERENCE",
                    categoriaPadre.getIdCategoria()
            );
            return;
        }

        if (categoriaPadre.aceptaProductos()) {
            errors.add(
                    "categoriaPadre",
                    "La categoría padre seleccionada es una categoría final y no puede contener subcategorías.",
                    "PARENT_CATEGORY_IS_SELECTABLE",
                    categoriaPadre.getIdCategoria()
            );
        }
    }

    private void validateDuplicates(
            boolean duplicatedCodigo,
            boolean duplicatedSiblingName,
            boolean duplicatedSlug
    ) {
        if (duplicatedCodigo) {
            throw new ConflictException(
                    "CATEGORIA_CODIGO_DUPLICADO",
                    "Ya existe una categoría con el mismo código generado."
            );
        }

        if (duplicatedSiblingName) {
            throw new ConflictException(
                    "CATEGORIA_NOMBRE_DUPLICADO_EN_PADRE",
                    "Ya existe una categoría activa con el mismo nombre dentro de la categoría padre seleccionada."
            );
        }

        if (duplicatedSlug) {
            throw new ConflictException(
                    "CATEGORIA_SLUG_DUPLICADO",
                    "Ya existe una categoría con el mismo slug."
            );
        }
    }
}
