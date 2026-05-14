// ruta: src/main/java/com/upsjb/ms3/dto/shared/EntityReferenceDto.java
package com.upsjb.ms3.dto.shared;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EntityReferenceDto(
        Long id,

        @Size(max = 100, message = "El código no debe superar 100 caracteres.")
        String codigo,

        @Size(max = 250, message = "El nombre no debe superar 250 caracteres.")
        String nombre,

        @Size(max = 250, message = "El slug no debe superar 250 caracteres.")
        String slug,

        @Size(max = 100, message = "El barcode no debe superar 100 caracteres.")
        String barcode,

        @Size(max = 30, message = "El número de documento no debe superar 30 caracteres.")
        String numeroDocumento,

        @Size(max = 20, message = "El RUC no debe superar 20 caracteres.")
        String ruc,

        Long idUsuarioMs1,

        Long idEmpleadoMs2,

        Long idEmpleadoSnapshot,

        @Size(max = 50, message = "El código de empleado no debe superar 50 caracteres.")
        String codigoEmpleado,

        @Size(max = 100, message = "El código SKU no debe superar 100 caracteres.")
        String codigoSku,

        @Size(max = 80, message = "El código de producto no debe superar 80 caracteres.")
        String codigoProducto,

        @Size(max = 50, message = "El código de almacén no debe superar 50 caracteres.")
        String codigoAlmacen,

        @Size(max = 80, message = "El código de proveedor no debe superar 80 caracteres.")
        String codigoProveedor,

        @Size(max = 80, message = "El código de promoción no debe superar 80 caracteres.")
        String codigoPromocion,

        @Size(max = 80, message = "El código de reserva no debe superar 80 caracteres.")
        String codigoReserva,

        @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
        String referenciaIdExterno
) {
}