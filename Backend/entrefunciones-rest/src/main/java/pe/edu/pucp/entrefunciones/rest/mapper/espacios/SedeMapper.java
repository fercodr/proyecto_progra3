package pe.edu.pucp.entrefunciones.rest.mapper.espacios;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;
import pe.edu.pucp.entrefunciones.rest.dto.espacios.SalaDTO;
import pe.edu.pucp.entrefunciones.rest.dto.espacios.SedeDTO;

import java.util.ArrayList;
import java.util.List;

public final class SedeMapper {

    private SedeMapper() {
    }

    public static SedeDTO toDTO(Sede sede) {
        SedeDTO dto = new SedeDTO();
        dto.setId(sede.getId());
        dto.setActivo(sede.estaActivo());
        dto.setRazonSocial(sede.getRazonSocial());
        dto.setDireccion(sede.getDireccion());
        dto.setCiudad(sede.getCiudad());
        dto.setDistrito(sede.getDistrito());
        dto.setTelefonoContacto(sede.getTelefonoContacto());
        return dto;
    }

    /** Igual que toDTO pero incluye las salas (usar con ?completo=true). */
    public static SedeDTO toDTOCompleto(Sede sede) {
        SedeDTO dto = toDTO(sede);
        dto.setSalas(SalaMapper.toDTOs(sede.getSalas()));
        return dto;
    }

    public static List<SedeDTO> toDTOs(List<Sede> sedes) {
        List<SedeDTO> dtos = new ArrayList<>();
        for (Sede sede : sedes) {
            dtos.add(toDTO(sede));
        }
        return dtos;
    }

    public static Sede toModelo(SedeDTO dto) throws BLException {
        Sede sede;
        try {
            sede = new Sede(dto.getId(), dto.getRazonSocial(), dto.getDireccion(),
                    dto.getCiudad(), dto.getTelefonoContacto());
            sede.setDistrito(dto.getDistrito());
            if (!dto.isActivo()) {
                sede.desactivar();
            }
            if (dto.getSalas() != null) {
                for (SalaDTO salaDTO : dto.getSalas()) {
                    Sala sala = new Sala(salaDTO.getId(), sede, salaDTO.getNombre(),
                            salaDTO.getAforo(), salaDTO.getTipoSala());
                    sala.setAccesible(salaDTO.isAccesible());
                    if (!salaDTO.isActivo()) {
                        sala.desactivar();
                    }
                    sede.agregarSala(sala);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new ValidacionException(e.getMessage());
        }
        return sede;
    }
}
