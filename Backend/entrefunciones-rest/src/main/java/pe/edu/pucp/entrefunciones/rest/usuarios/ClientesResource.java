package pe.edu.pucp.entrefunciones.rest.usuarios;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.impl.usuarios.ClienteBLImpl;
import pe.edu.pucp.entrefunciones.bl.usuarios.ClienteBL;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;
import pe.edu.pucp.entrefunciones.rest.dto.usuarios.ClienteDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.usuarios.ClienteMapper;

import java.util.Map;

/**
 * Clientes:
 *   POST /clientes  -> registro de un cliente nuevo (P02)
 *                      201 con el cliente creado | 400 si los datos no son válidos
 */
@Path("/clientes")
@Produces("application/json")
@Consumes("application/json")
public class ClientesResource {

    private final ClienteBL clienteBL;

    public ClientesResource() {
        clienteBL = new ClienteBLImpl();
    }

    @POST
    public Response registrar(ClienteDTO dto) {
        try {
            if (dto == null) {
                throw new ValidacionException("Envíe los datos del cliente");
            }

            // Un cliente nuevo: sin id, activo y con fecha de registro = hoy
            dto.setId(0);
            dto.setActivo(true);
            dto.setFechaRegistro(null);

            // DTO -> modelo (el mapper pone la fecha de hoy si viene vacía)
            Cliente cliente = ClienteMapper.toModelo(dto);

            // Valida (formato, correo y DNI únicos), cifra la contraseña y guarda
            clienteBL.insert(cliente);

            // salio todo bien
            return Response.status(Response.Status.CREATED)
                    .entity(ClienteMapper.toDTO(cliente))
                    .build();
        }
        catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    private Response error(Response.Status status, Exception ex) {
        return Response.status(status)
                .entity(Map.of("mensaje", ex.getMessage()))
                .build();
    }
}