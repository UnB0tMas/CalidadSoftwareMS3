// ruta: src/main/java/com/upsjb/ms3/mapper/ReferenceMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.dto.reference.response.AtributoOptionDto;
import com.upsjb.ms3.dto.reference.response.CategoriaOptionDto;
import com.upsjb.ms3.dto.reference.response.MarcaOptionDto;
import com.upsjb.ms3.dto.reference.response.TipoProductoOptionDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.IdCodigoNombreResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ReferenceMapper {

    public IdCodigoNombreResponseDto toIdCodigoNombre(TipoProducto entity) {
        if (entity == null) {
            return null;
        }

        return IdCodigoNombreResponseDto.builder()
                .id(entity.getIdTipoProducto())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .estado(entity.getEstado())
                .build();
    }

    public IdCodigoNombreResponseDto toIdCodigoNombre(Categoria entity) {
        if (entity == null) {
            return null;
        }

        return IdCodigoNombreResponseDto.builder()
                .id(entity.getIdCategoria())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .estado(entity.getEstado())
                .build();
    }

    public IdCodigoNombreResponseDto toIdCodigoNombre(Marca entity) {
        if (entity == null) {
            return null;
        }

        return IdCodigoNombreResponseDto.builder()
                .id(entity.getIdMarca())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .estado(entity.getEstado())
                .build();
    }

    public IdCodigoNombreResponseDto toIdCodigoNombre(Atributo entity) {
        if (entity == null) {
            return null;
        }

        return IdCodigoNombreResponseDto.builder()
                .id(entity.getIdAtributo())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .estado(entity.getEstado())
                .build();
    }

    public EntityReferenceDto toEntityReference(TipoProducto entity) {
        if (entity == null) {
            return null;
        }

        return EntityReferenceDto.builder()
                .id(entity.getIdTipoProducto())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .build();
    }

    public EntityReferenceDto toEntityReference(Categoria entity) {
        if (entity == null) {
            return null;
        }

        return EntityReferenceDto.builder()
                .id(entity.getIdCategoria())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .build();
    }

    public EntityReferenceDto toEntityReference(Marca entity) {
        if (entity == null) {
            return null;
        }

        return EntityReferenceDto.builder()
                .id(entity.getIdMarca())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .build();
    }

    public EntityReferenceDto toEntityReference(Atributo entity) {
        if (entity == null) {
            return null;
        }

        return EntityReferenceDto.builder()
                .id(entity.getIdAtributo())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .build();
    }

    public TipoProductoOptionDto toTipoProductoOption(TipoProducto entity) {
        if (entity == null) {
            return null;
        }

        return TipoProductoOptionDto.builder()
                .idTipoProducto(entity.getIdTipoProducto())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .estado(entity.getEstado())
                .build();
    }

    public CategoriaOptionDto toCategoriaOption(Categoria entity) {
        if (entity == null) {
            return null;
        }

        Categoria padre = entity.getCategoriaPadre();

        return CategoriaOptionDto.builder()
                .idCategoria(entity.getIdCategoria())
                .idCategoriaPadre(padre == null ? null : padre.getIdCategoria())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .nivel(entity.getNivel())
                .orden(entity.getOrden())
                .estado(entity.getEstado())
                .build();
    }

    public MarcaOptionDto toMarcaOption(Marca entity) {
        if (entity == null) {
            return null;
        }

        return MarcaOptionDto.builder()
                .idMarca(entity.getIdMarca())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .slug(entity.getSlug())
                .estado(entity.getEstado())
                .build();
    }

    public AtributoOptionDto toAtributoOption(Atributo entity) {
        if (entity == null) {
            return null;
        }

        return AtributoOptionDto.builder()
                .idAtributo(entity.getIdAtributo())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .tipoDato(entity.getTipoDato())
                .tipoDatoLabel(tipoDatoLabel(entity.getTipoDato()))
                .unidadMedida(entity.getUnidadMedida())
                .requerido(entity.getRequerido())
                .filtrable(entity.getFiltrable())
                .visiblePublico(entity.getVisiblePublico())
                .estado(entity.getEstado())
                .build();
    }

    public String tipoDatoLabel(TipoDatoAtributo tipoDato) {
        return tipoDato == null ? null : tipoDato.getLabel();
    }
}