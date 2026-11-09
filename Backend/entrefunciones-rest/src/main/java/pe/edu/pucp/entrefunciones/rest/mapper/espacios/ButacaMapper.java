package pe.edu.pucp.entrefunciones.rest.mapper.espacios;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.espacios.SalaBL;
import pe.edu.pucp.entrefunciones.bl.impl.espacios.SalaBLImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.rest.dto.espacios.ButacaDTO;

import java.util.ArrayList;
import java.util.List;

public final class ButacaMapper {
    private static final SalaBL salaBL = new SalaBLImpl();

    private ButacaMapper() {
    }

    public static ButacaDTO toDTO(Butaca butaca) {
        ButacaDTO dto = new ButacaDTO();
        dto.setId(butaca.getId());
        dto.setActivo(butaca.estaActivo());
        dto.setIdSala(butaca.getSala().getId());
        dto.setFila(butaca.getFila());
        dto.setColumna(butaca.getColumna());
        dto.setTipoButaca(butaca.getTipoButaca());
        return dto;
    }

    public static List<ButacaDTO> toDTOs(List<Butaca> butacas) {
        List<ButacaDTO> dtos = new ArrayList<>();
        for (Butaca butaca : butacas) {
            dtos.add(toDTO(butaca));
        }
        return dtos;
    }

    public static Butaca toModelo(ButacaDTO dto) throws BLException {
        Sala sala;
        try {
            sala = salaBL.findById(dto.getIdSala());
        } catch (NoEncontradoException e) {
            throw new ValidacionException("No existe la sala con id " + dto.getIdSala());
        }

        Butaca butaca;
        try {
            butaca = new Butaca(dto.getId(), sala, dto.getFila(), dto.getColumna(), dto.getTipoButaca());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException(e.getMessage());
        }
        if (!dto.isActivo()) {
            butaca.desactivar();
        }
        return butaca;
    }
}
