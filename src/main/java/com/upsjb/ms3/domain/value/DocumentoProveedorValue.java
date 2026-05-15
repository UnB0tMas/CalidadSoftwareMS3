package com.upsjb.ms3.domain.value;

import com.upsjb.ms3.domain.enums.TipoDocumentoProveedor;
import java.io.Serializable;
import java.util.Objects;

public record DocumentoProveedorValue(
        TipoDocumentoProveedor tipoDocumento,
        String numeroDocumento
) implements Serializable {

    public DocumentoProveedorValue {
        if (tipoDocumento == null) {
            throw new IllegalArgumentException("El tipo de documento del proveedor es obligatorio.");
        }

        numeroDocumento = normalize(numeroDocumento);

        if (numeroDocumento.length() < tipoDocumento.getMinLength()
                || numeroDocumento.length() > tipoDocumento.getMaxLength()) {
            throw new IllegalArgumentException(
                    "El número de documento no cumple la longitud permitida para " + tipoDocumento.getLabel() + "."
            );
        }

        if (tipoDocumento.isSoloNumeros() && !numeroDocumento.matches("^\\d+$")) {
            throw new IllegalArgumentException("El número de documento debe contener solo números.");
        }

        if (!tipoDocumento.isSoloNumeros() && !numeroDocumento.matches("^[A-Z0-9-]+$")) {
            throw new IllegalArgumentException("El número de documento tiene caracteres no permitidos.");
        }
    }

    public static DocumentoProveedorValue of(TipoDocumentoProveedor tipoDocumento, String numeroDocumento) {
        return new DocumentoProveedorValue(tipoDocumento, numeroDocumento);
    }

    public String raw() {
        return numeroDocumento;
    }

    public String display() {
        return tipoDocumento.getCode() + " " + numeroDocumento;
    }

    private static String normalize(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new IllegalArgumentException("El número de documento del proveedor es obligatorio.");
        }

        return numeroDocumento.trim()
                .replaceAll("\\s+", "")
                .toUpperCase();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof DocumentoProveedorValue that)) {
            return false;
        }

        return tipoDocumento == that.tipoDocumento
                && numeroDocumento.equals(that.numeroDocumento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoDocumento, numeroDocumento);
    }

    @Override
    public String toString() {
        return display();
    }
}