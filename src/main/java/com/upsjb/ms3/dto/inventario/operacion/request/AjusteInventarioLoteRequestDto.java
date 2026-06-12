// ruta: src/main/java/com/upsjb/ms3/dto/inventario/operacion/request/AjusteInventarioLoteRequestDto.java
package com.upsjb.ms3.dto.inventario.operacion.request;

import com.upsjb.ms3.dto.inventario.movimiento.request.AjusteInventarioRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Builder
public record AjusteInventarioLoteRequestDto(

        @Valid
        @NotEmpty(message = "Debe agregar al menos un producto al ajuste.")
        @Size(max = 200, message = "No puede procesar más de 200 productos por operación.")
        List<AjusteInventarioRequestDto> lineas
) {
}
