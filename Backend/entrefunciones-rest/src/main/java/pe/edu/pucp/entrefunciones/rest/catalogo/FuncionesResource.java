package pe.edu.pucp.entrefunciones.rest.catalogo;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.eventos.EventoBL;
import pe.edu.pucp.entrefunciones.bl.eventos.FuncionBL;
import pe.edu.pucp.entrefunciones.bl.impl.eventos.EventoBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.eventos.FuncionBLImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.rest.dto.catalogo.FuncionDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.catalogo.FuncionMapper;

import java.net.URI;
import java.util.Map;

/**
 * Programacion de funciones (fecha, horario, sala) para un evento del catalogo.
 * Pantalla de Programacion (admin). Para construir el modelo se busca primero el
 * Evento real con idEvento (via EventoBL) en vez de reconstruirlo desde el DTO, ya
 * que FuncionDTO solo trae el id del evento al que pertenece.
 */
@Path("/funciones")
@Produces("application/json")
@Consumes("application/json")
public class FuncionesResource {
    private final FuncionBL funcionBL;
    private final EventoBL eventoBL;

    @Context
    private UriInfo uriInfo;

    public FuncionesResource() {
        funcionBL = new FuncionBLImpl();
        eventoBL = new EventoBLImpl();
    }

    @GET
    public Response findAll() {
        try {
            return Response.ok(FuncionMapper.toDTOs(funcionBL.findAll()))
                    .build();
        } catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") int id) {
        try {
            Funcion funcion = funcionBL.findById(id);
            if (funcion == null) {
                return error(Response.Status.NOT_FOUND, new BLException("No existe una función con id " + id));
            }
            return Response.ok(FuncionMapper.toDTO(funcion)).build();
        } catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @POST
    public Response insert(FuncionDTO funcionDTO) {
        try {
            Evento evento = eventoBL.findById(funcionDTO.getIdEvento());
            if (evento == null) {
                return error(Response.Status.BAD_REQUEST,
                        new BLException("No existe un evento con id " + funcionDTO.getIdEvento()));
            }

            Funcion funcion = FuncionMapper.toModelo(funcionDTO, evento);
            funcionBL.insert(funcion);
            funcionDTO = FuncionMapper.toDTO(funcion);

            URI uri = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(funcion.getId()))
                    .build();

            return Response.created(uri)
                    .entity(funcionDTO)
                    .build();
        } catch (BLException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, FuncionDTO funcionDTO) {
        try {
            if (id != funcionDTO.getId()) {
                return error(Response.Status.BAD_REQUEST, new BLException("El id y el payload no coinciden"));
            }

            if (funcionBL.findById(id) == null) {
                return error(Response.Status.NOT_FOUND, new BLException("No existe una función con id " + id));
            }

            Evento evento = eventoBL.findById(funcionDTO.getIdEvento());
            if (evento == null) {
                return error(Response.Status.BAD_REQUEST,
                        new BLException("No existe un evento con id " + funcionDTO.getIdEvento()));
            }

            Funcion funcion = FuncionMapper.toModelo(funcionDTO, evento);
            funcionBL.update(funcion);

            return Response.ok(FuncionMapper.toDTO(funcion)).build();
        } catch (BLException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        }
    }

    // DELETE /funciones/{id} -> cancela la funcion (no la elimina fisicamente, ver FuncionDAOImpl)
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        try {
            if (funcionBL.findById(id) == null) {
                return error(Response.Status.NOT_FOUND, new BLException("No existe una función con id " + id));
            }

            funcionBL.delete(id);
            return Response.noContent().build();
        } catch (BLException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        }
    }

    private Response error(Response.Status status, Exception ex) {
        return Response.status(status)
                .entity(Map.of("error", ex.getMessage()))
                .build();
    }
}
