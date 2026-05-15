// ruta: src/main/java/com/upsjb/ms3/dto/empleado/filter/EmpleadoInventarioPermisoFilterDto.java
package com.upsjb.ms3.dto.empleado.filter;

import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EmpleadoInventarioPermisoFilterDto(

        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,

        Long idEmpleadoSnapshot,

        Long idEmpleadoMs2,

        Long idUsuarioMs1,

        String codigoEmpleado,

        String areaCodigo,

        Boolean empleadoActivo,

        Boolean vigente,

        Boolean puedeCrearProductoBasico,

        Boolean puedeEditarProductoBasico,

        Boolean puedeRegistrarEntrada,

        Boolean puedeRegistrarSalida,

        Boolean puedeRegistrarAjuste,

        Boolean puedeConsultarKardex,

        Boolean puedeGestionarImagenes,

        Boolean puedeActualizarAtributos,

        Boolean estado,

        @Valid
        DateRangeFilterDto fechaVigencia,

        @Valid
        DateRangeFilterDto fechaCreacion
) {
}