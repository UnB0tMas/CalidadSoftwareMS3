// ruta: src/main/java/com/upsjb/ms3/dto/inventario/operacion/request/EntradaInventarioLoteRequestDto.java
package com.upsjb.ms3.dto.inventario.operacion.request;

import com.upsjb.ms3.dto.inventario.movimiento.request.EntradaInventarioRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Builder
public record EntradaInventarioLoteRequestDto(

        @Valid
        @NotEmpty(message = "Debe agregar al menos un producto al ingreso.")
        @Size(max = 200, message = "No puede procesar más de 200 productos por operación.")
        List<EntradaInventarioRequestDto> lineas
) {
}
