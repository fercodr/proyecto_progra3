package pe.edu.pucp.entrefunciones.rest.mapper.espacios;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.espacios.SedeBL;
import pe.edu.pucp.entrefunciones.bl.impl.espacios.SedeBLImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;
import pe.edu.pucp.entrefunciones.rest.dto.espacios.ButacaDTO;
import pe.edu.pucp.entrefunciones.rest.dto.espacios.SalaDTO;

import java.util.ArrayList;
import java.util.List;

public final class SalaMapper {
    private static final SedeBL sedeBL = new SedeBLImpl();

    private SalaMapper() {
    }

    public static SalaDTO toDTO(Sala sala) {
        SalaDTO dto = new SalaDTO();
        dto.setId(sala.getId());
        dto.setActivo(sala.estaActivo());
        dto.setIdSede(sala.getSede().getId());
        dto.setNombre(sala.getNombre());
        dto.setAforo(sala.getAforo());
        dto.setTipoSala(sala.getTipoSala());
        dto.setAccesible(sala.esAccesible());
        return dto;
    }

    public static List<SalaDTO> toDTOs(List<Sala> salas) {
        List<SalaDTO> dtos = new ArrayList<>();
        for (Sala sala : salas) {
            dtos.add(toDTO(sala));
        }
        return dtos;
    }

    public static Sala toModelo(SalaDTO dto) throws BLException {
        Sede sede;
        try {
            sede = sedeBL.findById(dto.getIdSede());
        } catch (NoEncontradoException e) {
            throw new ValidacionException("No existe la sede con id " + dto.getIdSede());
        }

        Sala sala;
        try {
            sala = new Sala(dto.getId(), sede, dto.getNombre(), dto.getAforo(), dto.getTipoSala());
            sala.setAccesible(dto.isAccesible());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException(e.getMessage());
        }
        if (!dto.isActivo()) {
            sala.desactivar();
        }

        // Butacas que vienen junto con la sala (al crearla con su mapa).
        // SalaDAOImpl.insert las guarda con insertButacas, una vez que la sala ya tiene id.
        if (dto.getButacas() != null) {
            for (ButacaDTO b : dto.getButacas()) {
                try {
                    // Se crean activas: C# manda "isActivo" y Java espera "activo",
                    // así que ese dato no llega y quedarían todas inactivas.
                    sala.agregarButaca(new Butaca(0, sala, b.getFila(), b.getColumna(), b.getTipoButaca()));
                } catch (IllegalArgumentException e) {
                    throw new ValidacionException("Butaca " + b.getFila() + b.getColumna() + ": " + e.getMessage());
                }
            }
        }
        return sala;
    }
}
