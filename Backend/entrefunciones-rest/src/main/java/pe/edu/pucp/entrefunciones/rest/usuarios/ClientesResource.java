package pe.edu.pucp.entrefunciones.rest.usuarios;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.impl.usuarios.ClienteBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.usuarios.CuentaUsuarioBLImpl;
import pe.edu.pucp.entrefunciones.bl.usuarios.CuentaUsuarioBL;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;
import pe.edu.pucp.entrefunciones.rest.dto.usuarios.CambiarPasswordDTO;
import pe.edu.pucp.entrefunciones.rest.dto.usuarios.ClienteDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.usuarios.ClienteMapper;

import java.util.List;
import java.util.Map;


@Path("/clientes")
@Produces("application/json")
@Consumes("application/json")
public class ClientesResource {

    // Se usa ClienteBLImpl (y no la interfaz) porque su insert solo lanza BLException
    private final ClienteBLImpl clienteBL;
    private final CuentaUsuarioBL cuentaUsuarioBL;

    public ClientesResource() {
        clienteBL = new ClienteBLImpl();
        cuentaUsuarioBL = new CuentaUsuarioBLImpl();
    }

    // registro
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

            Cliente cliente = ClienteMapper.toModelo(dto);
            clienteBL.insert(cliente);   // valida, cifra la contraseña y guarda

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

    // listar clientes
    @GET
    public Response listar() {
        try {
            List<Cliente> clientes = clienteBL.findAll();
            return Response.ok(ClienteMapper.toDTOs(clientes)).build();
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    // ver perfil-
    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") int id) {
        try {
            return Response.ok(ClienteMapper.toDTO(buscar(id))).build();
        }
        catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    // editar datos
    @PUT
    @Path("/{id}")
    public Response actualizar(@PathParam("id") int id, ClienteDTO dto) {
        try {
            if (dto == null) {
                throw new ValidacionException("Envíe los datos del cliente");
            }
            // Se parte del cliente guardado: así se conservan la contraseña (hash),
            // el DNI, la fecha de registro y el estado. Solo cambian los datos editables.
            Cliente cliente = buscar(id);
            cliente.setNombres(dto.getNombres());
            cliente.setApellidos(dto.getApellidos());
            cliente.setCorreo(dto.getCorreo());
            cliente.setTelefono(dto.getTelefono());

            clienteBL.update(cliente);
            return Response.ok(ClienteMapper.toDTO(cliente)).build();
        }
        catch (IllegalArgumentException ex) {
            // Los setters del modelo rechazan valores vacíos
            return error(Response.Status.BAD_REQUEST, ex);
        }
        catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        }
        catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    // cambiar contraseña
    @PUT
    @Path("/{id}/password")
    public Response cambiarContrasena(@PathParam("id") int id, CambiarPasswordDTO dto) {
        try {
            if (dto == null) {
                throw new ValidacionException("Envíe la contraseña actual y la nueva");
            }
            cuentaUsuarioBL.cambiarContrasena(id, dto.getContrasenaActual(), dto.getContrasenaNueva());
            return Response.noContent().build();
        }
        catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        }
        catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    // desactiva
    @DELETE
    @Path("/{id}")
    public Response desactivar(@PathParam("id") int id) {
        try {
            clienteBL.delete(id);
            return Response.noContent().build();
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    // volver a activar
    @POST
    @Path("/{id}/activar")
    public Response activar(@PathParam("id") int id) {
        try {
            Cliente cliente = buscar(id);
            cliente.activar();
            clienteBL.update(cliente);
            return Response.noContent().build();
        }
        catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    // auxiliar
    private Cliente buscar(int id) throws BLException {
        Cliente cliente = clienteBL.findById(id);
        if (cliente == null) {
            throw new NoEncontradoException("No existe el cliente con id " + id);
        }
        return cliente;
    }

    private Response error(Response.Status status, Exception ex) {
        return Response.status(status)
                .entity(Map.of("mensaje", ex.getMessage()))
                .build();
    }
}