package pe.edu.pucp.entrefunciones.rest.espacios;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.impl.espacios.BloqueoSalaBLImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.BloqueoSala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.rest.AutorizacionRest;
import pe.edu.pucp.entrefunciones.rest.dto.espacios.BloqueoSalaDTO;

@Path("/bloqueos-sala")
@Produces("application/json")
@Consumes("application/json")
public class BloqueosSalaResource {
    private final BloqueoSalaBLImpl bl = new BloqueoSalaBLImpl();
    private final SalaDAOImpl salas = new SalaDAOImpl();

    @Context private SecurityContext sesion;

    @GET
    public Response listar() {
        AutorizacionRest.exigirAdministrador(sesion);
        try {
            List<BloqueoSalaDTO> resultado = new ArrayList<>();
            for (BloqueoSala bloqueo : bl.findAll()) resultado.add(dto(bloqueo));
            return Response.ok(resultado).build();
        } catch (BLException ex) { return error(Response.Status.INTERNAL_SERVER_ERROR, ex); }
    }

    @GET
    @Path("/disponibilidad")
    public Response disponibilidad(@QueryParam("salaId") int salaId,
                                   @QueryParam("fecha") String fecha,
                                   @QueryParam("horaInicio") String horaInicio,
                                   @QueryParam("horaFin") String horaFin,
                                   @QueryParam("excluirFuncionId") Integer excluirFuncionId) {
        AutorizacionRest.exigirAdministrador(sesion);
        try {
            var resultado = bl.consultar(salaId, LocalDate.parse(fecha),
                    LocalTime.parse(horaInicio), LocalTime.parse(horaFin), excluirFuncionId);
            return Response.ok(Map.of(
                    "disponible", resultado.disponible(),
                    "motivo", resultado.motivo())).build();
        } catch (IllegalArgumentException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        } catch (BLException ex) { return error(Response.Status.BAD_REQUEST, ex); }
    }

    @POST
    public Response insertar(BloqueoSalaDTO solicitud) {
        AutorizacionRest.exigirAdministrador(sesion);
        try {
            BloqueoSala bloqueo = modelo(solicitud);
            bloqueo.setId(0);
            bloqueo.activar();
            return Response.status(Response.Status.CREATED).entity(dto(bl.insert(bloqueo))).build();
        } catch (Exception ex) { return error(Response.Status.BAD_REQUEST, ex); }
    }

    @PUT
    @Path("/{id}")
    public Response actualizar(@PathParam("id") int id, BloqueoSalaDTO solicitud) {
        AutorizacionRest.exigirAdministrador(sesion);
        try {
            if (solicitud == null || id != solicitud.getId())
                return error(Response.Status.BAD_REQUEST, new Exception("El id y el payload no coinciden"));
            return Response.ok(dto(bl.update(modelo(solicitud)))).build();
        } catch (Exception ex) { return error(Response.Status.BAD_REQUEST, ex); }
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        AutorizacionRest.exigirAdministrador(sesion);
        try { bl.delete(id); return Response.noContent().build(); }
        catch (BLException ex) { return error(Response.Status.BAD_REQUEST, ex); }
    }

    private BloqueoSala modelo(BloqueoSalaDTO dto) throws Exception {
        if (dto == null) throw new IllegalArgumentException("Indique el bloqueo");
        Sala sala = salas.findById(dto.getSalaId());
        if (sala == null) throw new IllegalArgumentException("La sala no existe");
        BloqueoSala bloqueo = new BloqueoSala(dto.getId(), sala, dto.getInicio(), dto.getFin());
        if (!dto.isActivo()) bloqueo.desactivar();
        return bloqueo;
    }

    private BloqueoSalaDTO dto(BloqueoSala bloqueo) {
        BloqueoSalaDTO dto = new BloqueoSalaDTO();
        dto.setId(bloqueo.getId());
        dto.setSalaId(bloqueo.getSala().getId());
        dto.setSalaNombre(bloqueo.getSala().getNombre());
        dto.setSedeNombre(bloqueo.getSala().getSede().getRazonSocial());
        dto.setInicio(bloqueo.getInicio());
        dto.setFin(bloqueo.getFin());
        dto.setActivo(bloqueo.estaActivo());
        return dto;
    }

    private Response error(Response.Status estado, Exception ex) {
        return Response.status(estado).entity(Map.of("mensaje", ex.getMessage())).build();
    }
}
