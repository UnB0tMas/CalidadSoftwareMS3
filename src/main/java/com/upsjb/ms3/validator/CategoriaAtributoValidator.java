package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.CategoriaAtributo;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import org.springframework.stereotype.Component;

@Component
public class CategoriaAtributoValidator {

    public void validateAssign(
            Categoria categoria,
            Atributo atributo,
            Integer orden,
            boolean duplicatedAssociation
    ) {
        ValidationErrorCollector errors =
                ValidationErrorCollector.create();

        validateCategoria(
                categoria,
                errors
        );

        validateAtributo(
                atributo,
                errors
        );

        validateOrden(
                orden,
                errors
        );

        errors.throwIfAny(
                "No se puede asociar el atributo a la categoría."
        );

        if (duplicatedAssociation) {
            throw new ConflictException(
                    "CATEGORIA_ATRIBUTO_DUPLICADO",
                    "El atributo ya está asociado a la categoría."
            );
        }
    }

    public void validateUpdate(
            CategoriaAtributo relation,
            Integer orden
    ) {
        requireActive(relation);

        if (
                orden != null
                        && orden < 0
        ) {
            throw new ConflictException(
                    "ORDEN_INVALIDO",
                    "El orden no puede ser negativo."
            );
        }
    }

    public void validateCanActivate(
            CategoriaAtributo relation,
            boolean duplicatedActiveAssociation
    ) {
        requireExists(relation);

        if (relation.isActivo()) {
            throw new ConflictException(
                    "CATEGORIA_ATRIBUTO_YA_ACTIVO",
                    "La asociación ya se encuentra activa."
            );
        }

        ValidationErrorCollector errors =
                ValidationErrorCollector.create();

        validateCategoria(
                relation.getCategoria(),
                errors
        );

        validateAtributo(
                relation.getAtributo(),
                errors
        );

        errors.throwIfAny(
                "No se puede activar la asociación."
        );

        if (duplicatedActiveAssociation) {
            throw new ConflictException(
                    "CATEGORIA_ATRIBUTO_DUPLICADO",
                    "Ya existe una asociación activa para la misma categoría y atributo."
            );
        }
    }

    public void validateCanRemove(
            CategoriaAtributo relation,
            boolean hasProductOrSkuValues
    ) {
        requireActive(relation);

        if (hasProductOrSkuValues) {
            throw new ConflictException(
                    "ATRIBUTO_ASOCIADO_CON_VALORES",
                    "No se puede quitar la asociación porque existen productos o SKU con valores registrados."
            );
        }
    }

    public void requireActive(
            CategoriaAtributo relation
    ) {
        requireExists(relation);

        if (!relation.isActivo()) {
            throw new NotFoundException(
                    "CATEGORIA_ATRIBUTO_INACTIVO",
                    "La asociación entre categoría y atributo no está activa."
            );
        }
    }

    public void requireExists(
            CategoriaAtributo relation
    ) {
        if (relation == null) {
            throw new NotFoundException(
                    "CATEGORIA_ATRIBUTO_NO_ENCONTRADO",
                    "No se encontró la asociación solicitada."
            );
        }
    }

    private void validateCategoria(
            Categoria categoria,
            ValidationErrorCollector errors
    ) {
        if (categoria == null) {
            errors.add(
                    "categoria",
                    "La categoría es obligatoria.",
                    "REQUIRED",
                    null
            );

            return;
        }

        if (!categoria.isActivo()) {
            errors.add(
                    "categoria",
                    "La categoría debe estar activa.",
                    "INACTIVE",
                    categoria.getIdCategoria()
            );

            return;
        }
    }

    private void validateAtributo(
            Atributo atributo,
            ValidationErrorCollector errors
    ) {
        if (atributo == null) {
            errors.add(
                    "atributo",
                    "El atributo es obligatorio.",
                    "REQUIRED",
                    null
            );

            return;
        }

        if (!atributo.isActivo()) {
            errors.add(
                    "atributo",
                    "El atributo debe estar activo.",
                    "INACTIVE",
                    atributo.getIdAtributo()
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
                    "INVALID_VALUE",
                    orden
            );
        }
    }
}
