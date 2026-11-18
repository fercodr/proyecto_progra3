package pe.edu.pucp.entrefunciones.rest.catalogo;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.SecurityContext;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.eventos.EventoBL;
import pe.edu.pucp.entrefunciones.bl.impl.eventos.EventoBLImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.rest.dto.catalogo.EventoDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.catalogo.EventoMapper;
import pe.edu.pucp.entrefunciones.rest.AutorizacionRest;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;

import java.net.URI;
import java.time.LocalDateTime;
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

    @Context
    private SecurityContext securityContext;

    public EventosResource() {
        eventoBL = new EventoBLImpl();
    }

    @GET
    public Response findAll() {
        try {
            var eventos = eventoBL.findAll();
            if (!AutorizacionRest.esAdministrador(securityContext)) {
                eventos = eventos.stream()
                        .filter(Evento::estaActivo)
                        .filter(evento -> evento.getFunciones().stream()
                                .anyMatch(funcion -> funcion.getEstado() == EstadoFuncion.CONFIRMADA
                                        && funcion.getFecha().atTime(funcion.getHoraInicio())
                                                .isAfter(LocalDateTime.now())))
                        .toList();
            }
            return Response.ok(EventoMapper.toDTOs(eventos))
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

            if (!AutorizacionRest.esAdministrador(securityContext)) {
                evento.getFunciones().removeIf(
                        funcion -> funcion.getEstado() != EstadoFuncion.CONFIRMADA
                                || !funcion.getFecha().atTime(funcion.getHoraInicio())
                                        .isAfter(LocalDateTime.now()));
                if (!evento.estaActivo() || evento.getFunciones().isEmpty()) {
                    return error(Response.Status.NOT_FOUND,
                            new BLException("No existe un evento publicado con id " + id));
                }
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
        AutorizacionRest.exigirAdministrador(securityContext);
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
        AutorizacionRest.exigirAdministrador(securityContext);
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
        AutorizacionRest.exigirAdministrador(securityContext);
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
        if (status.getStatusCode() >= 500) {
            java.util.logging.Logger.getLogger(EventosResource.class.getName())
                    .log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
        }
        return Response.status(status)
                .entity(Map.of("error", ex.getMessage()))
                .build();
    }
}
