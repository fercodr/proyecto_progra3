package pe.edu.pucp.entrefunciones.rest.usuarios;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.impl.usuarios.CuentaUsuarioBLImpl;
import pe.edu.pucp.entrefunciones.bl.usuarios.CuentaUsuarioBL;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Usuario;
import pe.edu.pucp.entrefunciones.rest.dto.usuarios.CredencialesDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.usuarios.AdministradorMapper;
import pe.edu.pucp.entrefunciones.rest.mapper.usuarios.ClienteMapper;
import pe.edu.pucp.entrefunciones.rest.mapper.usuarios.CuentaUsuarioMapper;

import java.util.Map;

@Path("/cuentas")
@Produces("application/json")
@Consumes("application/json")
public class CuentasResource {

    private final CuentaUsuarioBL cuentaUsuarioBL;

    public CuentasResource() {
        cuentaUsuarioBL = new CuentaUsuarioBLImpl();
    }

    // Autenticar
    @POST
    @Path("/login")
    public Response autenticar(CredencialesDTO credenciales) {
        try {
            if (credenciales == null) {
                throw new ValidacionException("Envíe correo y contraseña");
            }
            Usuario usuario = cuentaUsuarioBL.autenticar(
                    credenciales.getCorreo(), credenciales.getContrasena());

            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Correo o contraseña incorrectos"))
                        .build();
            }
            return Response.ok(CuentaUsuarioMapper.toDTO(usuario)).build();
        } catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        } catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    // Buscar la cuenta por correo
    @GET
    @Path("/{correo}")
    public Response findByCorreo(@PathParam("correo") String correo) {
        try {
            return Response.ok(CuentaUsuarioMapper.toDTO(cuentaUsuarioBL.findByCorreo(correo)))
                    .build();
        } catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
        } catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    // Es administrador?
    @GET
    @Path("/{correo}/administrador")
    public Response obtenerAdministrador(@PathParam("correo") String correo) {
        try {
            Usuario usuario = cuentaUsuarioBL.findByCorreo(correo);
            if (!(usuario instanceof Administrador admin)) {
                throw new NoEncontradoException("La cuenta no es de administrador");
            }
            return Response.ok(AdministradorMapper.toDTO(admin)).build();
        } catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
        } catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    // Es cliente?
    @GET
    @Path("/{correo}/cliente")
    public Response obtenerCliente(@PathParam("correo") String correo) {
        try {
            Usuario usuario = cuentaUsuarioBL.findByCorreo(correo);
            if (!(usuario instanceof Cliente cliente)) {
                throw new NoEncontradoException("La cuenta no es de cliente");
            }
            return Response.ok(ClienteMapper.toDTO(cliente)).build();
        } catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
        } catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    private Response error(Response.Status status, Exception ex) {
        return Response.status(status)
                .entity(Map.of("error", ex.getMessage()))
                .build();
    }
}