package pe.edu.pucp.entrefunciones.rest.espacios;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.espacios.SedeBL;
import pe.edu.pucp.entrefunciones.bl.impl.espacios.SedeBLImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;
import pe.edu.pucp.entrefunciones.rest.dto.espacios.SedeDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.espacios.SedeMapper;

import java.net.URI;
import java.util.Map;

@Path("/sedes")
@Produces("application/json")
@Consumes("application/json")
public class SedesResource {
    private final SedeBL sedeBL;

    @Context
    private UriInfo uriInfo;

    public SedesResource() {
        sedeBL = new SedeBLImpl();
    }

    @GET
    public Response findAll() {
        try {
            return Response.ok(SedeMapper.toDTOs(sedeBL.findAll()))
                    .build();
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") int id, @QueryParam("completo") @DefaultValue("false") boolean completo) {
        try {
            if (completo) {
                return Response.ok(SedeMapper.toDTOCompleto(sedeBL.findByIdCompleto(id)))
                        .build();
            }
            return Response.ok(SedeMapper.toDTO(sedeBL.findById(id)))
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
    public Response insert(SedeDTO sedeDTO) {
        try {
            sedeDTO.setActivo(true);
            Sede sede = SedeMapper.toModelo(sedeDTO);
            sedeBL.insert(sede);
            sedeDTO = SedeMapper.toDTO(sede);

            URI uri = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(sede.getId()))
                    .build();

            return Response.created(uri)
                    .entity(sedeDTO)
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
    public Response update(@PathParam("id") int id, SedeDTO sedeDTO) {
        try {
            if (id != sedeDTO.getId()) {
                throw new ValidacionException("El id y el payload no coinciden");
            }

            Sede sede = SedeMapper.toModelo(sedeDTO);
            sedeBL.update(sede);

            return Response.ok(SedeMapper.toDTO(sede))
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

    // PUT /sedes/{id}/salas -> sincroniza las salas de la sede con las del payload
    @PUT
    @Path("/{id}/salas")
    public Response sincronizarSalas(@PathParam("id") int id, SedeDTO sedeDTO) {
        try {
            if (id != sedeDTO.getId()) {
                throw new ValidacionException("El id y el payload no coinciden");
            }

            Sede sede = SedeMapper.toModelo(sedeDTO);
            sedeBL.sincronizarSalas(sede);

            return Response.ok(SedeMapper.toDTOCompleto(sedeBL.findByIdCompleto(id)))
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
        try {
            sedeBL.delete(id);
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
                .entity(Map.of("error", ex.getMessage()))
                .build();
    }
}
