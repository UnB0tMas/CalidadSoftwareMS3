// ruta: src/main/java/com/upsjb/ms3/dto/inventario/operacion/request/TransferenciaInventarioRequestDto.java
package com.upsjb.ms3.dto.inventario.operacion.request;

import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Builder
public record TransferenciaInventarioRequestDto(

        @Valid
        @NotNull(message = "El almacén de origen es obligatorio.")
        EntityReferenceDto almacenOrigen,

        @Valid
        @NotNull(message = "El almacén de destino es obligatorio.")
        EntityReferenceDto almacenDestino,

        @Valid
        @NotEmpty(message = "Debe agregar al menos un producto a la transferencia.")
        @Size(max = 200, message = "No puede transferir más de 200 productos por operación.")
        List<TransferenciaInventarioLineaRequestDto> lineas,

        @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
        String referenciaIdExterno,

        @Size(max = 500, message = "La observación no debe superar 500 caracteres.")
        String observacion
) {
}
