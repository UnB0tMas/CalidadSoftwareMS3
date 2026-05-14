// ruta: src/main/java/com/upsjb/ms3/dto/inventario/compra/request/CompraInventarioUpdateRequestDto.java
package com.upsjb.ms3.dto.inventario.compra.request;

import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record CompraInventarioUpdateRequestDto(

        @Valid
        @NotNull(message = "El proveedor es obligatorio.")
        EntityReferenceDto proveedor,

        LocalDateTime fechaCompra,

        @NotNull(message = "La moneda es obligatoria.")
        Moneda moneda,

        @Size(max = 500, message = "La observación no debe superar 500 caracteres.")
        String observacion,

        @Valid
        @NotEmpty(message = "La compra debe tener al menos un detalle.")
        List<CompraInventarioDetalleRequestDto> detalles
) {
}