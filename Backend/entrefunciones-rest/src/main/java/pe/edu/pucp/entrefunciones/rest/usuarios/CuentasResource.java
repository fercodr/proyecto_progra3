package pe.edu.pucp.entrefunciones.rest.usuarios;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.impl.usuarios.CuentaUsuarioBLImpl;
import pe.edu.pucp.entrefunciones.bl.usuarios.CuentaUsuarioBL;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Usuario;
import pe.edu.pucp.entrefunciones.rest.auth.JwtServicio;
import pe.edu.pucp.entrefunciones.rest.dto.usuarios.CredencialesDTO;
import pe.edu.pucp.entrefunciones.rest.dto.usuarios.RecuperarContrasenaDTO;
import pe.edu.pucp.entrefunciones.rest.dto.usuarios.SesionDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.usuarios.AdministradorMapper;
import pe.edu.pucp.entrefunciones.rest.mapper.usuarios.ClienteMapper;
import pe.edu.pucp.entrefunciones.rest.mapper.usuarios.CuentaUsuarioMapper;
import pe.edu.pucp.entrefunciones.rest.AutorizacionRest;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/cuentas")
@Produces("application/json")
@Consumes("application/json")
public class CuentasResource {
    private static final Logger LOGGER = Logger.getLogger(
            CuentasResource.class.getName());
    private final CuentaUsuarioBL cuentaUsuarioBL;

    @Context
    private SecurityContext securityContext;

    public CuentasResource() { cuentaUsuarioBL = new CuentaUsuarioBLImpl(); }

    @POST
    @Path("/login")
    public Response autenticar(CredencialesDTO credenciales) {
        try {
            if (credenciales == null) throw new ValidacionException("Envíe correo y contraseña");
            Usuario usuario = cuentaUsuarioBL.autenticar(credenciales.getCorreo(), credenciales.getContrasena());
            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("mensaje", "Correo o contraseña incorrectos")).build();
            }
            JwtServicio.Emision emision = JwtServicio.emitir(usuario);
            return Response.ok(new SesionDTO(emision.token(), emision.vence().toString(),
                    emision.id(), emision.correo(), emision.rol())).build();
        } catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        } catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.SEVERE,
                    "No se pudo emitir la sesion por configuracion invalida de JWT", ex);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of("mensaje",
                            "El servicio de autenticacion no esta disponible"))
                    .build();
        }
    }

    @GET
    @Path("/{correo}")
    public Response findByCorreo(@PathParam("correo") String correo) {
        try {
            Usuario usuario = cuentaUsuarioBL.findByCorreo(correo);
            AutorizacionRest.exigirPropietarioOAdministrador(securityContext, usuario.getId());
            return Response.ok(CuentaUsuarioMapper.toDTO(usuario)).build();
        } catch (NoEncontradoException ex) { return error(Response.Status.NOT_FOUND, ex); }
        catch (BLException ex) { return error(Response.Status.INTERNAL_SERVER_ERROR, ex); }
    }

    @GET
    @Path("/{correo}/administrador")
    public Response obtenerAdministrador(@PathParam("correo") String correo) {
        try {
            Usuario usuario = cuentaUsuarioBL.findByCorreo(correo);
            AutorizacionRest.exigirPropietarioOAdministrador(securityContext, usuario.getId());
            if (!(usuario instanceof Administrador admin))
                throw new NoEncontradoException("La cuenta no es de administrador");
            return Response.ok(AdministradorMapper.toDTO(admin)).build();
        } catch (NoEncontradoException ex) { return error(Response.Status.NOT_FOUND, ex); }
        catch (BLException ex) { return error(Response.Status.INTERNAL_SERVER_ERROR, ex); }
    }

    @GET
    @Path("/{correo}/cliente")
    public Response obtenerCliente(@PathParam("correo") String correo) {
        try {
            Usuario usuario = cuentaUsuarioBL.findByCorreo(correo);
            AutorizacionRest.exigirPropietarioOAdministrador(securityContext, usuario.getId());
            if (!(usuario instanceof Cliente cliente))
                throw new NoEncontradoException("La cuenta no es de cliente");
            return Response.ok(ClienteMapper.toDTO(cliente)).build();
        } catch (NoEncontradoException ex) { return error(Response.Status.NOT_FOUND, ex); }
        catch (BLException ex) { return error(Response.Status.INTERNAL_SERVER_ERROR, ex); }
    }

    @POST
    @Path("/recuperar")
    public Response recuperarContrasena(RecuperarContrasenaDTO dto) {
        try {
            if (dto == null) throw new ValidacionException("Envíe correo, DNI y la nueva contraseña");
            cuentaUsuarioBL.recuperarContrasena(dto.getCorreo(), dto.getDni(), dto.getContrasenaNueva());
            return Response.noContent().build();
        } catch (ValidacionException ex) { return error(Response.Status.BAD_REQUEST, ex); }
        catch (BLException ex) { return error(Response.Status.INTERNAL_SERVER_ERROR, ex); }
    }

    private Response error(Response.Status status, Exception ex) {
        return Response.status(status).entity(Map.of("mensaje", ex.getMessage())).build();
    }
}
