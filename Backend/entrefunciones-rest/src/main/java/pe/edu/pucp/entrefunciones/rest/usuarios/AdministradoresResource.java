package pe.edu.pucp.entrefunciones.rest.usuarios;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.impl.usuarios.AdministradorBLImpl;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;
import pe.edu.pucp.entrefunciones.rest.dto.usuarios.AdministradorDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.usuarios.AdministradorMapper;

import java.util.Map;

@Path("/administradores")
@Produces("application/json")
@Consumes("application/json")
public class AdministradoresResource {


    private final AdministradorBLImpl administradorBL;

    public AdministradoresResource() {
        administradorBL = new AdministradorBLImpl();
    }

    @GET
    public Response listar() {
        try {
            return Response.ok(AdministradorMapper.toDTOs(administradorBL.findAll())).build();
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @POST
    public Response registrar(AdministradorDTO dto) {
        try {
            if (dto == null) {
                throw new ValidacionException("Envíe los datos del administrador");
            }
            dto.setId(0);
            Administrador administrador = AdministradorMapper.toModelo(dto);
            if (!dto.isActivo()) {
                administrador.desactivar();
            }
            administradorBL.insert(administrador);   // valida, cifra la contraseña y guarda

            return Response.status(Response.Status.CREATED)
                    .entity(AdministradorMapper.toDTO(administrador))
                    .build();
        }
        catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    // Desactivar 
    @DELETE
    @Path("/{id}")
    public Response desactivar(@PathParam("id") int id) {
        try {
            administradorBL.delete(id);
            return Response.noContent().build();
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    // Volver a activar
    @POST
    @Path("/{id}/activar")
    public Response activar(@PathParam("id") int id) {
        try {
            Administrador administrador = administradorBL.findById(id);
            if (administrador == null) {
                throw new NoEncontradoException("No existe el administrador con id " + id);
            }
            administrador.activar();
            administradorBL.update(administrador);
            return Response.noContent().build();
        }
        catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
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