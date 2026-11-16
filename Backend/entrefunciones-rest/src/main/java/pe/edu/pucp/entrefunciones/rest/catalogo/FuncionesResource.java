package pe.edu.pucp.entrefunciones.rest.catalogo;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.SecurityContext;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.eventos.EventoBL;
import pe.edu.pucp.entrefunciones.bl.eventos.FuncionBL;
import pe.edu.pucp.entrefunciones.bl.impl.eventos.EventoBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.eventos.FuncionBLImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.rest.dto.catalogo.FuncionDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.catalogo.FuncionMapper;
import pe.edu.pucp.entrefunciones.rest.AutorizacionRest;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
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

    @Context
    private SecurityContext securityContext;

    public FuncionesResource() {
        funcionBL = new FuncionBLImpl();
        eventoBL = new EventoBLImpl();
    }

    @GET
    public Response findAll() {
        try {
            List<Funcion> funciones = funcionBL.findAll();
            if (!AutorizacionRest.esAdministrador(securityContext)) {
                funciones = funciones.stream()
                        .filter(funcion -> funcion.getEstado() == EstadoFuncion.CONFIRMADA)
                        .filter(funcion -> funcion.getEvento().estaActivo())
                        .filter(funcion -> funcion.getSala().estaActivo()
                                && funcion.getSala().getSede().estaActivo())
                        .toList();
            }
            return Response.ok(FuncionMapper.toDTOs(funciones))
                    .build();
        } catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @GET
    @Path("reportes")
    public Response reportes(@QueryParam("desde") String desdeTexto,
                             @QueryParam("hasta") String hastaTexto,
                             @QueryParam("sedeId") Integer sedeId,
                             @QueryParam("salaId") Integer salaId,
                             @QueryParam("tipoEvento") String tipoEvento) {
        if (securityContext == null
                || !securityContext.isUserInRole("Administrador")) {
            return error(Response.Status.FORBIDDEN,
                    new BLException("Se requiere administrador"));
        }
        try {
            if (desdeTexto == null || hastaTexto == null) {
                return error(Response.Status.BAD_REQUEST,
                        new BLException("Los parámetros desde y hasta son obligatorios"));
            }
            LocalDate desde = LocalDate.parse(desdeTexto);
            LocalDate hasta = LocalDate.parse(hastaTexto);
            List<FuncionDTO> resultado = FuncionMapper.toDTOs(
                    funcionBL.findReportes(desde, hasta, sedeId, salaId, tipoEvento));
            return Response.ok(resultado).build();
        } catch (DateTimeParseException ex) {
            return error(Response.Status.BAD_REQUEST,
                    new BLException("Las fechas deben tener formato yyyy-MM-dd"));
        } catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        } catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    new BLException("No se pudo consultar el reporte de funciones", ex));
        }
    }

    /**
     * Disponibilidad de butacas para comprar (pantalla Selección de butacas).
     * Vive aquí porque JAX-RS envía todo /funciones/... a este recurso y nunca
     * llegaría al método equivalente de VentasResource (@Path("/")).
     */
    @GET
    @Path("/{id}/disponibilidad")
    public jakarta.json.JsonObject disponibilidad(@PathParam("id") int id) throws Exception {
        return new pe.edu.pucp.entrefunciones.rest.VentasResource().disponibilidad(id);
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") int id) {
        try {
            Funcion funcion = funcionBL.findById(id);
            if (funcion == null
                    || (!AutorizacionRest.esAdministrador(securityContext)
                        && (funcion.getEstado() != EstadoFuncion.CONFIRMADA
                            || !funcion.getEvento().estaActivo()
                            || !funcion.getSala().estaActivo()
                            || !funcion.getSala().getSede().estaActivo()))) {
                return error(Response.Status.NOT_FOUND, new BLException("No existe una función con id " + id));
            }
            return Response.ok(FuncionMapper.toDTO(funcion)).build();
        } catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @POST
    public Response insert(FuncionDTO funcionDTO) {
        AutorizacionRest.exigirAdministrador(securityContext);
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
        AutorizacionRest.exigirAdministrador(securityContext);
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
        AutorizacionRest.exigirAdministrador(securityContext);
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
        if (status.getStatusCode() >= 500) {
            java.util.logging.Logger.getLogger(FuncionesResource.class.getName())
                    .log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
        }
        return Response.status(status)
                .entity(Map.of("mensaje", ex.getMessage()))
                .build();
    }
}
