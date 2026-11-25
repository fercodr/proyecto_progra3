package pe.edu.pucp.entrefunciones.rest.catalogo;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.SecurityContext;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.eventos.EventoBL;
import pe.edu.pucp.entrefunciones.bl.eventos.FuncionBL;
import pe.edu.pucp.entrefunciones.bl.espacios.SalaBL;
import pe.edu.pucp.entrefunciones.bl.espacios.SedeBL;
import pe.edu.pucp.entrefunciones.bl.impl.eventos.EventoBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.eventos.FuncionBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.espacios.SalaBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.espacios.SedeBLImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;
import pe.edu.pucp.entrefunciones.rest.dto.catalogo.FuncionDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.catalogo.FuncionMapper;
import pe.edu.pucp.entrefunciones.rest.AutorizacionRest;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;
import pe.edu.pucp.entrefunciones.rest.reportes.ReporteFuncionesPdf;
import net.sf.jasperreports.engine.JRException;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
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
    private final SedeBL sedeBL;
    private final SalaBL salaBL;
    private final ReporteFuncionesPdf reportePdf;
    private final pe.edu.pucp.entrefunciones.rest.reportes.ReporteDetalleEventoPdf reportePdfDetalle;
    private static final DateTimeFormatter FECHA_REPORTE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    public FuncionesResource() {
        funcionBL = new FuncionBLImpl();
        eventoBL = new EventoBLImpl();
        sedeBL = new SedeBLImpl();
        salaBL = new SalaBLImpl();
        reportePdf = new ReporteFuncionesPdf();
        reportePdfDetalle = new pe.edu.pucp.entrefunciones.rest.reportes.ReporteDetalleEventoPdf();
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
                        // Una funcion confirmada que ya empezo no se debe seguir mostrando en
                        // Cartelera como comprable; el historial se conserva igual para Reportes,
                        // solo se oculta de este listado publico.
                        .filter(funcion -> funcion.getFecha().atTime(funcion.getHoraInicio())
                                .isAfter(LocalDateTime.now()))
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
            LocalDate desde = parseFechaOpcional(desdeTexto);
            LocalDate hasta = parseFechaOpcional(hastaTexto);
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

    @GET
    @Path("reportes/ocupacion/pdf")
    @Produces("application/pdf")
    public Response reporteOcupacionPdf(
            @QueryParam("desde") String desdeTexto,
            @QueryParam("hasta") String hastaTexto,
            @QueryParam("sedeId") Integer sedeId,
            @QueryParam("salaId") Integer salaId) {
        if (!AutorizacionRest.esAdministrador(securityContext)) {
            return error(Response.Status.FORBIDDEN,
                    new BLException("Se requiere administrador"));
        }
        try {
            LocalDate desde = parseFechaOpcional(desdeTexto);
            LocalDate hasta = parseFechaOpcional(hastaTexto);
            List<Funcion> funciones = funcionBL.findReportes(
                    desde, hasta, sedeId, salaId, null);
            byte[] contenido = reportePdf.generarOcupacion(
                    funciones,
                    filtrosOcupacion(desde, hasta, sedeId, salaId, funciones));
            return pdf(contenido, "reporte-ocupacion-funciones.pdf");
        } catch (DateTimeParseException ex) {
            return error(Response.Status.BAD_REQUEST,
                    new BLException("Las fechas deben tener formato yyyy-MM-dd"));
        } catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        } catch (BLException | JRException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    new BLException("No se pudo generar el reporte de ocupación", ex));
        }
    }

    @GET
    @Path("reportes/ingresos/pdf")
    @Produces("application/pdf")
    public Response reporteIngresosPdf(
            @QueryParam("desde") String desdeTexto,
            @QueryParam("hasta") String hastaTexto,
            @QueryParam("sedeId") Integer sedeId,
            @QueryParam("tipoEvento") String tipoEvento) {
        if (!AutorizacionRest.esAdministrador(securityContext)) {
            return error(Response.Status.FORBIDDEN,
                    new BLException("Se requiere administrador"));
        }
        try {
            LocalDate desde = parseFechaOpcional(desdeTexto);
            LocalDate hasta = parseFechaOpcional(hastaTexto);
            List<Funcion> funciones = funcionBL.findReportes(
                    desde, hasta, sedeId, null, tipoEvento);
            byte[] contenido = reportePdf.generarIngresos(
                    funciones,
                    filtrosIngresos(desde, hasta, sedeId, tipoEvento, funciones));
            return pdf(contenido, "reporte-ingresos-cancelaciones.pdf");
        } catch (DateTimeParseException ex) {
            return error(Response.Status.BAD_REQUEST,
                    new BLException("Las fechas deben tener formato yyyy-MM-dd"));
        } catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        } catch (BLException | JRException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    new BLException("No se pudo generar el reporte de ingresos y cancelaciones", ex));
        }
    }

    @GET
    @Path("reportes/ocupacion/detalle/pdf")
    @Produces("application/pdf")
    public Response reporteOcupacionDetallePdf(
            @QueryParam("eventoId") Integer eventoId) {
        if (!AutorizacionRest.esAdministrador(securityContext)) {
            return error(Response.Status.FORBIDDEN,
                    new BLException("Se requiere administrador"));
        }
        if (eventoId == null) {
            return error(Response.Status.BAD_REQUEST,
                    new BLException("Debe indicar el evento"));
        }
        try (java.sql.Connection conexion =
                pe.edu.pucp.entrefunciones.db.DBManager.getInstance().getConnection()) {
            byte[] contenido = reportePdfDetalle.generarOcupacion(eventoId, conexion);
            return pdf(contenido,
                    "reporte-ocupacion-detalle-evento-" + eventoId + ".pdf");
        } catch (java.sql.SQLException | JRException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    new BLException("No se pudo generar el detalle de ocupación", ex));
        }
    }

    @GET
    @Path("reportes/ingresos/detalle/pdf")
    @Produces("application/pdf")
    public Response reporteIngresosDetallePdf(
            @QueryParam("eventoId") Integer eventoId) {
        if (!AutorizacionRest.esAdministrador(securityContext)) {
            return error(Response.Status.FORBIDDEN,
                    new BLException("Se requiere administrador"));
        }
        if (eventoId == null) {
            return error(Response.Status.BAD_REQUEST,
                    new BLException("Debe indicar el evento"));
        }
        try (java.sql.Connection conexion =
                pe.edu.pucp.entrefunciones.db.DBManager.getInstance().getConnection()) {
            byte[] contenido = reportePdfDetalle.generarIngresos(eventoId, conexion);
            return pdf(contenido,
                    "reporte-ingresos-detalle-evento-" + eventoId + ".pdf");
        } catch (java.sql.SQLException | JRException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    new BLException("No se pudo generar el detalle de ingresos", ex));
        }
    }

    private Response pdf(byte[] contenido, String nombreArchivo) {
        return Response.ok(contenido, "application/pdf")
                .header("Content-Disposition",
                        "attachment; filename=\"" + nombreArchivo + "\"")
                .build();
    }

    private String filtrosOcupacion(LocalDate desde, LocalDate hasta,
                                    Integer sedeId, Integer salaId,
                                    List<Funcion> funciones) throws BLException {
        return periodo(desde, hasta)
                + " | Sede: " + nombreSede(sedeId, funciones)
                + " | Sala: " + nombreSala(salaId, funciones);
    }

    private String filtrosIngresos(LocalDate desde, LocalDate hasta,
                                   Integer sedeId, String tipoEvento,
                                   List<Funcion> funciones) throws BLException {
        return periodo(desde, hasta)
                + " | Sede: " + nombreSede(sedeId, funciones)
                + " | Tipo de evento: " + nombreTipoEvento(tipoEvento);
    }

    private String periodo(LocalDate desde, LocalDate hasta) {
        if (desde == null && hasta == null) {
            return "Periodo: todo";
        }
        if (desde == null) {
            return "Periodo: hasta " + hasta.format(FECHA_REPORTE);
        }
        if (hasta == null) {
            return "Periodo: desde " + desde.format(FECHA_REPORTE);
        }
        return "Periodo: del " + desde.format(FECHA_REPORTE)
                + " al " + hasta.format(FECHA_REPORTE);
    }

    private String nombreSede(Integer sedeId, List<Funcion> funciones)
            throws BLException {
        if (sedeId == null) {
            return "Todas";
        }
        for (Funcion funcion : funciones) {
            Sede sede = funcion.getSala().getSede();
            if (sede.getId() == sedeId) {
                return sede.getRazonSocial();
            }
        }
        Sede sede = sedeBL.findById(sedeId);
        return sede == null ? "Sede " + sedeId : sede.getRazonSocial();
    }

    private String nombreSala(Integer salaId, List<Funcion> funciones)
            throws BLException {
        if (salaId == null) {
            return "Todas";
        }
        for (Funcion funcion : funciones) {
            Sala sala = funcion.getSala();
            if (sala.getId() == salaId) {
                return sala.getNombre();
            }
        }
        Sala sala = salaBL.findById(salaId);
        return sala == null ? "Sala " + salaId : sala.getNombre();
    }

    private String nombreTipoEvento(String tipoEvento) {
        if (tipoEvento == null || tipoEvento.isBlank()) {
            return "Todos";
        }
        return switch (tipoEvento.trim().toUpperCase(Locale.ROOT)) {
            case "PELICULA" -> "Película";
            case "OBRA_TEATRO" -> "Obra de teatro";
            case "ACTIVIDAD_CULTURAL" -> "Actividad cultural";
            default -> tipoEvento.trim();
        };
    }

    private LocalDate parseFechaOpcional(String valor) {
        return valor == null || valor.isBlank()
                ? null
                : LocalDate.parse(valor);
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
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("mensaje", ex.getMessage()))
                .build();
    }
}
