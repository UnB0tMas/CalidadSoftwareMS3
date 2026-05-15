// ruta: src/main/java/com/upsjb/ms3/dto/proveedor/response/ProveedorResponseDto.java
package com.upsjb.ms3.dto.proveedor.response;

import com.upsjb.ms3.domain.enums.TipoDocumentoProveedor;
import com.upsjb.ms3.domain.enums.TipoProveedor;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ProveedorResponseDto(
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
        String correo,
        String telefono,
        String direccion,
        String observacion,
        Long creadoPorIdUsuarioMs1,
        Long actualizadoPorIdUsuarioMs1,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}