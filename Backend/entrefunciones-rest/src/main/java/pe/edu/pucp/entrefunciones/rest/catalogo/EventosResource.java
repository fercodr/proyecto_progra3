package pe.edu.pucp.entrefunciones.rest.catalogo;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.eventos.EventoBL;
import pe.edu.pucp.entrefunciones.bl.impl.eventos.EventoBLImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.rest.dto.catalogo.EventoDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.catalogo.EventoMapper;

import java.net.URI;
import java.util.Map;

/**
 * Catalogo de eventos (peliculas, obras de teatro, actividades culturales) que se
 * muestra en Cartelera (cliente) y se administra en Catalogo/Programacion (admin).
 *
 * Nota: EventoBLImpl solo lanza BLException "plano" (no usa ValidacionException ni
 * NoEncontradoException como si hace SedeBLImpl), asi que aqui el 404 se decide
 * explicitamente comprobando si el evento existe antes de actualizar/eliminar, y
 * cualquier BLException al insertar/actualizar se trata como error de validacion (400).
 */
@Path("/eventos")
@Produces("application/json")
@Consumes("application/json")
public class EventosResource {
    private final EventoBL eventoBL;

    @Context
    private UriInfo uriInfo;

    public EventosResource() {
        eventoBL = new EventoBLImpl();
    }

    @GET
    public Response findAll() {
        try {
            return Response.ok(EventoMapper.toDTOs(eventoBL.findAll()))
                    .build();
        } catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") int id, @QueryParam("completo") @DefaultValue("false") boolean completo) {
        try {
            Evento evento = eventoBL.findById(id);
            if (evento == null) {
                return error(Response.Status.NOT_FOUND, new BLException("No existe un evento con id " + id));
            }

            if (completo) {
                return Response.ok(EventoMapper.toDTOCompleto(evento)).build();
            }
            return Response.ok(EventoMapper.toDTO(evento)).build();
        } catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @POST
    public Response insert(EventoDTO eventoDTO) {
        try {
            eventoDTO.setActivo(true);
            Evento evento = EventoMapper.toModelo(eventoDTO);
            eventoBL.insert(evento);
            eventoDTO = EventoMapper.toDTOCompleto(evento);

            URI uri = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(evento.getId()))
                    .build();

            return Response.created(uri)
                    .entity(eventoDTO)
                    .build();
        } catch (BLException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, EventoDTO eventoDTO) {
        try {
            if (id != eventoDTO.getId()) {
                return error(Response.Status.BAD_REQUEST, new BLException("El id y el payload no coinciden"));
            }

            if (eventoBL.findById(id) == null) {
                return error(Response.Status.NOT_FOUND, new BLException("No existe un evento con id " + id));
            }

            Evento evento = EventoMapper.toModelo(eventoDTO);
            eventoBL.update(evento);

            return Response.ok(EventoMapper.toDTOCompleto(evento)).build();
        } catch (BLException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        try {
            if (eventoBL.findById(id) == null) {
                return error(Response.Status.NOT_FOUND, new BLException("No existe un evento con id " + id));
            }

            eventoBL.delete(id);
            return Response.noContent().build();
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
