// ruta: src/main/java/com/upsjb/ms3/dto/reference/response/ProveedorOptionDto.java
package com.upsjb.ms3.dto.reference.response;

import com.upsjb.ms3.domain.enums.TipoDocumentoProveedor;
import com.upsjb.ms3.domain.enums.TipoProveedor;
import lombok.Builder;

@Builder
public record ProveedorOptionDto(
        Long idProveedor,
        TipoProveedor tipoProveedor,
        TipoDocumentoProveedor tipoDocumento,
        String numeroDocumento,
        String ruc,
        String razonSocial,
        String nombreComercial,
        String nombres,
        String apellidos,
        String displayName,
        Boolean estado
) {
}