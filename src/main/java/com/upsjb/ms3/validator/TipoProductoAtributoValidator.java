// ruta: src/main/java/com/upsjb/ms3/validator/TipoProductoAtributoValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.domain.entity.TipoProductoAtributo;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import org.springframework.stereotype.Component;

@Component
public class TipoProductoAtributoValidator {

    public void validateAssign(
            TipoProducto tipoProducto,
            Atributo atributo,
            Integer orden,
            boolean duplicatedAssociation
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (tipoProducto == null) {
            errors.add("tipoProducto", "El tipo de producto es obligatorio.", "REQUIRED", null);
        } else if (!tipoProducto.isActivo()) {
            errors.add("tipoProducto", "El tipo de producto debe estar activo.", "INACTIVE", tipoProducto.getIdTipoProducto());
        }

        if (atributo == null) {
            errors.add("atributo", "El atributo es obligatorio.", "REQUIRED", null);
        } else if (!atributo.isActivo()) {
            errors.add("atributo", "El atributo debe estar activo.", "INACTIVE", atributo.getIdAtributo());
        }

        if (orden != null && orden < 0) {
            errors.add("orden", "El orden no puede ser negativo.", "INVALID_VALUE", orden);
        }

        errors.throwIfAny("No se puede asociar el atributo al tipo de producto.");

        if (duplicatedAssociation) {
            throw new ConflictException(
                    "TIPO_PRODUCTO_ATRIBUTO_DUPLICADO",
                    "El atributo ya está asociado al tipo de producto."
            );
        }
    }

    public void validateUpdate(TipoProductoAtributo relation, Integer orden) {
        requireActive(relation);

        if (orden != null && orden < 0) {
            throw new ConflictException(
                    "ORDEN_INVALIDO",
                    "El orden no puede ser negativo."
            );
        }
    }

    public void validateCanRemove(TipoProductoAtributo relation, boolean hasProductOrSkuValues) {
        requireActive(relation);

        if (hasProductOrSkuValues) {
            throw new ConflictException(
                    "ATRIBUTO_ASOCIADO_CON_VALORES",
                    "No se puede quitar la asociación porque existen productos o SKU con valores registrados."
            );
        }
    }

    public void requireActive(TipoProductoAtributo relation) {
        if (relation == null) {
            throw new NotFoundException(
                    "TIPO_PRODUCTO_ATRIBUTO_NO_ENCONTRADO",
                    "Asociación de tipo de producto y atributo no encontrada."
            );
        }

        if (!relation.isActivo()) {
            throw new NotFoundException(
                    "TIPO_PRODUCTO_ATRIBUTO_INACTIVO",
                    "La asociación de tipo de producto y atributo no está activa."
            );
        }
    }
}