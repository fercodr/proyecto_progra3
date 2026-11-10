package pe.edu.pucp.entrefunciones.rest.espacios;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.espacios.ButacaBL;
import pe.edu.pucp.entrefunciones.bl.impl.espacios.ButacaBLImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.rest.dto.espacios.ButacaDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.espacios.ButacaMapper;

import java.net.URI;
import java.util.Map;

@Path("/butacas")
@Produces("application/json")
@Consumes("application/json")
public class ButacasResource {
    private final ButacaBL butacaBL;

    @Context
    private UriInfo uriInfo;

    public ButacasResource() {
        butacaBL = new ButacaBLImpl();
    }

    // GET /butacas  |  /butacas?idSala=1  |  /butacas?idReserva=5
    @GET
    public Response findAll(@QueryParam("idSala") Integer idSala,
                            @QueryParam("idReserva") Integer idReserva) {
        try {
            if (idSala != null) {
                return Response.ok(ButacaMapper.toDTOs(butacaBL.listarPorSala(idSala)))
                        .build();
            }
            if (idReserva != null) {
                return Response.ok(ButacaMapper.toDTOs(butacaBL.listarPorReserva(idReserva)))
                        .build();
            }
            return Response.ok(ButacaMapper.toDTOs(butacaBL.findAll()))
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
            return Response.ok(ButacaMapper.toDTO(butacaBL.findById(id)))
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
    public Response insert(ButacaDTO butacaDTO) {
        try {
            butacaDTO.setActivo(true);
            Butaca butaca = ButacaMapper.toModelo(butacaDTO);
            butacaBL.insert(butaca);
            butacaDTO = ButacaMapper.toDTO(butaca);

            URI uri = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(butaca.getId()))
                    .build();

            return Response.created(uri)
                    .entity(butacaDTO)
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
    public Response update(@PathParam("id") int id, ButacaDTO butacaDTO) {
        try {
            if (id != butacaDTO.getId()) {
                throw new ValidacionException("El id y el payload no coinciden");
            }

            Butaca butaca = ButacaMapper.toModelo(butacaDTO);
            butacaBL.update(butaca);

            return Response.ok(ButacaMapper.toDTO(butaca))
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
            butacaBL.delete(id);
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
