package pe.edu.pucp.entrefunciones.rest.mapper.usuarios;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;
import pe.edu.pucp.entrefunciones.rest.dto.usuarios.AdministradorDTO;

import java.util.ArrayList;
import java.util.List;

public final class AdministradorMapper {

    private AdministradorMapper() {
    }

    /** Modelo → DTO. La contraseña NO se copia. */
    public static AdministradorDTO toDTO(Administrador administrador) {
        AdministradorDTO dto = new AdministradorDTO();
        dto.setId(administrador.getId());
        dto.setActivo(administrador.estaActivo());
        dto.setNombres(administrador.getNombres());
        dto.setApellidos(administrador.getApellidos());
        dto.setCorreo(administrador.getCorreo());
        dto.setDni(administrador.getDni());
        dto.setCodigoEmpleado(administrador.getCodigoEmpleado());
        return dto;
    }

    public static List<AdministradorDTO> toDTOs(List<Administrador> administradores) {
        List<AdministradorDTO> dtos = new ArrayList<>();
        for (Administrador administrador : administradores) {
            dtos.add(toDTO(administrador));
        }
        return dtos;
    }

    /** DTO → modelo. El DTO debe traer la contraseña (ver nota en ClienteMapper.toModelo). */
    public static Administrador toModelo(AdministradorDTO dto) throws BLException {
        try {
            Administrador administrador = new Administrador(dto.getId(), dto.getNombres(), dto.getApellidos(),
                    dto.getCorreo(), dto.getContrasena(), dto.getDni(), dto.getCodigoEmpleado());
            if (!dto.isActivo() && dto.getId() > 0) {
                administrador.desactivar();
            }
            return administrador;
        } catch (IllegalArgumentException e) {
            throw new ValidacionException(e.getMessage());
        }
    }
}