package pe.edu.pucp.entrefunciones.rest.mapper.usuarios;

import pe.edu.pucp.entrefunciones.modelo.usuarios.Usuario;
import pe.edu.pucp.entrefunciones.rest.dto.usuarios.CuentaUsuarioDTO;

import java.util.ArrayList;
import java.util.List;

/** Datos de la cuenta (sirve para Cliente y Administrador). Nunca incluye la contraseña. */
public final class CuentaUsuarioMapper {

    private CuentaUsuarioMapper() {
    }

    public static CuentaUsuarioDTO toDTO(Usuario usuario) {
        CuentaUsuarioDTO dto = new CuentaUsuarioDTO();
        dto.setId(usuario.getId());
        dto.setActivo(usuario.estaActivo());
        dto.setCorreo(usuario.getCorreo());
        return dto;
    }

    public static List<CuentaUsuarioDTO> toDTOs(List<? extends Usuario> usuarios) {
        List<CuentaUsuarioDTO> dtos = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            dtos.add(toDTO(usuario));
        }
        return dtos;
    }
}