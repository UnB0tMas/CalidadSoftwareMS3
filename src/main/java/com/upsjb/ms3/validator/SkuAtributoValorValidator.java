// ruta: src/main/java/com/upsjb/ms3/validator/SkuAtributoValorValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.SkuAtributoValor;
import com.upsjb.ms3.domain.entity.TipoProductoAtributo;
import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.util.StringNormalizer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;

@Component
public class SkuAtributoValorValidator {

    private static final int MAX_VALOR_TEXTO_LENGTH = 500;

    public void validateAssociationAllowed(
            ProductoSku sku,
            Atributo atributo,
            TipoProductoAtributo relation
    ) {
        requireSkuWithProductType(sku);
        requireActiveAttribute(atributo);

        if (relation == null || !relation.isActivo()) {
            throw new ConflictException(
                    "SKU_ATRIBUTO_NO_PERMITIDO",
                    "El atributo no está asociado al tipo de producto del SKU."
            );
        }
    }

    public void validateValueByTemplate(
            Atributo atributo,
            TipoProductoAtributo relation,
            String valorTexto,
            BigDecimal valorNumero,
            Boolean valorBoolean,
            LocalDate valorFecha
    ) {
        requireActiveAttribute(atributo);

        if (StringNormalizer.hasText(valorTexto) && StringNormalizer.clean(valorTexto).length() > MAX_VALOR_TEXTO_LENGTH) {
            throw new ValidationException(
                    "SKU_ATRIBUTO_VALOR_TEXTO_INVALIDO",
                    "El valor texto no debe superar 500 caracteres."
            );
        }

        if (isRequired(atributo, relation) && !hasAnyValue(valorTexto, valorNumero, valorBoolean, valorFecha)) {
            throw new ConflictException(
                    "SKU_ATRIBUTO_VALOR_REQUERIDO",
                    "El atributo " + atributo.getNombre() + " es obligatorio para el SKU."
            );
        }

        if (atributo.getTipoDato() == TipoDatoAtributo.NUMERO
                && valorNumero != null
                && valorNumero.stripTrailingZeros().scale() > 0) {
            throw new ConflictException(
                    "SKU_ATRIBUTO_NUMERO_INVALIDO",
                    "El atributo " + atributo.getNombre() + " requiere un número entero."
            );
        }
    }

    public void validateDuplicateInReplacement(
            Long idAtributo,
            Set<Long> atributosProcesados
    ) {
        if (idAtributo == null) {
            throw new ValidationException(
                    "ATRIBUTO_ID_REQUERIDO",
                    "Debe indicar el atributo."
            );
        }

        if (atributosProcesados != null && !atributosProcesados.add(idAtributo)) {
            throw new ConflictException(
                    "SKU_ATRIBUTO_DUPLICADO",
                    "No se puede registrar el mismo atributo más de una vez para el SKU."
            );
        }
    }

    public void validateRequiredAttributesPresent(
            List<TipoProductoAtributo> plantilla,
            Set<Long> atributosProcesados
    ) {
        if (plantilla == null || plantilla.isEmpty()) {
            return;
        }

        StringJoiner faltantes = new StringJoiner(", ");

        for (TipoProductoAtributo relation : plantilla) {
            if (relation == null || relation.getAtributo() == null) {
                continue;
            }

            Atributo atributo = relation.getAtributo();
            Long idAtributo = atributo.getIdAtributo();

            if (isRequired(atributo, relation)
                    && (atributosProcesados == null || !atributosProcesados.contains(idAtributo))) {
                faltantes.add(atributo.getNombre() == null ? atributo.getCodigo() : atributo.getNombre());
            }
        }

        String faltantesText = faltantes.toString();
        if (!faltantesText.isBlank()) {
            throw new ConflictException(
                    "SKU_ATRIBUTOS_REQUERIDOS_FALTANTES",
                    "No se puede reemplazar los atributos porque faltan atributos obligatorios: " + faltantesText + "."
            );
        }
    }

    public void validateCanInactivate(
            SkuAtributoValor valor,
            TipoProductoAtributo relation
    ) {
        requireActiveValue(valor);

        Atributo atributo = valor.getAtributo();

        if (atributo != null && isRequired(atributo, relation)) {
            throw new ConflictException(
                    "SKU_ATRIBUTO_REQUERIDO_NO_INACTIVABLE",
                    "No se puede inactivar el valor porque el atributo es obligatorio para el SKU."
            );
        }
    }

    public void requireSkuWithProductType(ProductoSku sku) {
        if (sku == null || !sku.isActivo()) {
            throw new NotFoundException(
                    "SKU_NO_ENCONTRADO",
                    "No se encontró el registro solicitado."
            );
        }

        Producto producto = sku.getProducto();
        if (producto == null || !producto.isActivo()) {
            throw new ConflictException(
                    "PRODUCTO_INVALIDO_PARA_SKU_ATRIBUTO",
                    "No se puede registrar atributos porque el producto del SKU no está activo."
            );
        }

        if (producto.getTipoProducto() == null || producto.getTipoProducto().getIdTipoProducto() == null) {
            throw new ConflictException(
                    "PRODUCTO_SIN_TIPO",
                    "No se puede registrar atributos porque el producto no tiene tipo configurado."
            );
        }
    }

    private void requireActiveAttribute(Atributo atributo) {
        if (atributo == null || !atributo.isActivo()) {
            throw new NotFoundException(
                    "ATRIBUTO_NO_ENCONTRADO",
                    "No se encontró el registro solicitado."
            );
        }
    }

    private void requireActiveValue(SkuAtributoValor valor) {
        if (valor == null || !valor.isActivo()) {
            throw new NotFoundException(
                    "SKU_ATRIBUTO_VALOR_NO_ENCONTRADO",
                    "No se encontró el registro solicitado."
            );
        }
    }

    private boolean isRequired(Atributo atributo, TipoProductoAtributo relation) {
        return (atributo != null && Boolean.TRUE.equals(atributo.getRequerido()))
                || (relation != null && Boolean.TRUE.equals(relation.getRequerido()));
    }

    private boolean hasAnyValue(
            String valorTexto,
            BigDecimal valorNumero,
            Boolean valorBoolean,
            LocalDate valorFecha
    ) {
        return StringNormalizer.hasText(valorTexto)
                || valorNumero != null
                || valorBoolean != null
                || valorFecha != null;
    }
}