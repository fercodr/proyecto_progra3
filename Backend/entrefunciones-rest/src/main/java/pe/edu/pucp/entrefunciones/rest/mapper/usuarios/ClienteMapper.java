package pe.edu.pucp.entrefunciones.rest.mapper.usuarios;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;
import pe.edu.pucp.entrefunciones.rest.dto.usuarios.ClienteDTO;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class ClienteMapper {

    private ClienteMapper() {
    }

    /** Modelo → DTO. La contraseña NO se copia: nunca sale en una respuesta. */
    public static ClienteDTO toDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setActivo(cliente.estaActivo());
        dto.setNombres(cliente.getNombres());
        dto.setApellidos(cliente.getApellidos());
        dto.setCorreo(cliente.getCorreo());
        dto.setDni(cliente.getDni());
        dto.setTelefono(cliente.getTelefono());
        dto.setFechaRegistro(cliente.getFechaRegistro() == null ? null : cliente.getFechaRegistro().toString());
        return dto;
    }

    public static List<ClienteDTO> toDTOs(List<Cliente> clientes) {
        List<ClienteDTO> dtos = new ArrayList<>();
        for (Cliente cliente : clientes) {
            dtos.add(toDTO(cliente));
        }
        return dtos;
    }

    /**
     * DTO → modelo (registro o modificación).
     * Si el DTO no trae fecha de registro (registro nuevo), se usa la fecha de hoy.
     * El DTO debe traer la contraseña: al modificar el perfil, el recurso REST la completa
     * con la contraseña actual antes de llamar a este método.
     */
    public static Cliente toModelo(ClienteDTO dto) throws BLException {
        try {
            LocalDate fechaRegistro = (dto.getFechaRegistro() == null || dto.getFechaRegistro().isEmpty())
                    ? LocalDate.now()
                    : LocalDate.parse(dto.getFechaRegistro());
            Cliente cliente = new Cliente(dto.getId(), dto.getNombres(), dto.getApellidos(),
                    dto.getCorreo(), dto.getContrasena(), dto.getDni(), fechaRegistro, dto.getTelefono());
            if (!dto.isActivo() && dto.getId() > 0) {
                cliente.desactivar();
            }
            return cliente;
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new ValidacionException(e.getMessage());
        }
    }
}