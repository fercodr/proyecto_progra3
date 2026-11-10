package pe.edu.pucp.entrefunciones.rest.espacios;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.espacios.SalaBL;
import pe.edu.pucp.entrefunciones.bl.impl.espacios.SalaBLImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.rest.dto.espacios.SalaDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.espacios.SalaMapper;

import java.net.URI;
import java.util.Map;

@Path("/salas")
@Produces("application/json")
@Consumes("application/json")
public class SalasResource {
    private final SalaBL salaBL;

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    public SalasResource() {
        salaBL = new SalaBLImpl();
    }

    // GET /salas  o  GET /salas?idSede=1
    @GET
    public Response findAll(@QueryParam("idSede") Integer idSede) {
        try {
            if (idSede != null) {
                return Response.ok(SalaMapper.toDTOs(salaBL.listarPorSede(idSede)))
                        .build();
            }
            return Response.ok(SalaMapper.toDTOs(salaBL.findAll()))
                    .build();
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") int id) {
        try {
            return Response.ok(SalaMapper.toDTO(salaBL.findById(id)))
                    .build();
        }
        catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @POST
    public Response insert(SalaDTO salaDTO) {
        Response prohibido = soloAdministrador();
        if (prohibido != null) {
            return prohibido;
        }
        try {
            salaDTO.setActivo(true);
            Sala sala = SalaMapper.toModelo(salaDTO);
            salaBL.insert(sala);
            salaDTO = SalaMapper.toDTO(sala);

            URI uri = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(sala.getId()))
                    .build();

            return Response.created(uri)
                    .entity(salaDTO)
                    .build();
        }
        catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, SalaDTO salaDTO) {
        Response prohibido = soloAdministrador();
        if (prohibido != null) {
            return prohibido;
        }
        try {
            if (id != salaDTO.getId()) {
                throw new ValidacionException("El id y el payload no coinciden");
            }

            Sala sala = SalaMapper.toModelo(salaDTO);
            salaBL.update(sala);

            return Response.ok(SalaMapper.toDTO(sala))
                    .build();
        }
        catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
        }
        catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        Response prohibido = soloAdministrador();
        if (prohibido != null) {
            return prohibido;
        }
        try {
            salaBL.delete(id);
            return Response.noContent().build();
        }
        catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    private Response soloAdministrador() {
        if (securityContext.isUserInRole("Administrador")) {
            return null;
        }
        return error(Response.Status.FORBIDDEN,
                new Exception("Solo un administrador puede modificar salas"));
    }

    private Response error(Response.Status status, Exception ex) {
        return Response.status(status)
                .entity(Map.of("error", ex.getMessage()))
                .build();
    }
}
