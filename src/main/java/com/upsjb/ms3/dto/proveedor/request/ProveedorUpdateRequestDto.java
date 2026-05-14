// ruta: src/main/java/com/upsjb/ms3/dto/proveedor/request/ProveedorUpdateRequestDto.java
package com.upsjb.ms3.dto.proveedor.request;

import com.upsjb.ms3.domain.enums.TipoDocumentoProveedor;
import com.upsjb.ms3.domain.enums.TipoProveedor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProveedorUpdateRequestDto(

        @NotNull(message = "El tipo de proveedor es obligatorio.")
        TipoProveedor tipoProveedor,

        TipoDocumentoProveedor tipoDocumento,

        @Size(max = 30, message = "El número de documento no debe superar 30 caracteres.")
        String numeroDocumento,

        @Size(max = 20, message = "El RUC no debe superar 20 caracteres.")
        String ruc,

        @Size(max = 200, message = "La razón social no debe superar 200 caracteres.")
        String razonSocial,

        @Size(max = 200, message = "El nombre comercial no debe superar 200 caracteres.")
        String nombreComercial,

        @Size(max = 150, message = "Los nombres no deben superar 150 caracteres.")
        String nombres,

        @Size(max = 150, message = "Los apellidos no deben superar 150 caracteres.")
        String apellidos,

        @Email(message = "El correo no tiene formato válido.")
        @Size(max = 180, message = "El correo no debe superar 180 caracteres.")
        String correo,

        @Size(max = 30, message = "El teléfono no debe superar 30 caracteres.")
        String telefono,

        @Size(max = 300, message = "La dirección no debe superar 300 caracteres.")
        String direccion,

        @Size(max = 500, message = "La observación no debe superar 500 caracteres.")
        String observacion
) {
}