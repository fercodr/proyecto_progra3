package pe.edu.pucp.entrefunciones.rest;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import pe.edu.pucp.entrefunciones.bl.auditoria.AuditoriaBL;
import pe.edu.pucp.entrefunciones.bl.impl.auditoria.AuditoriaBLImpl;
import pe.edu.pucp.entrefunciones.modelo.auditoria.Auditoria;
import pe.edu.pucp.entrefunciones.modelo.auditoria.EstadoAuditoria;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;
import pe.edu.pucp.entrefunciones.rest.dto.auditoria.ActualizarAuditoriaRequestDTO;
import pe.edu.pucp.entrefunciones.rest.dto.auditoria.AdministradorResumenDTO;
import pe.edu.pucp.entrefunciones.rest.dto.auditoria.AuditoriaResponseDTO;
import pe.edu.pucp.entrefunciones.rest.dto.auditoria.CrearAuditoriaRequestDTO;
import pe.edu.pucp.entrefunciones.rest.dto.auditoria.FuncionResumenDTO;

@Path("auditorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditoriaResource {

    @Context
    private SecurityContext sesion;

    private final AuditoriaBL auditorias = new AuditoriaBLImpl();

    @GET
    public List<AuditoriaResponseDTO> listar(
            @QueryParam("descripcion") String descripcion,
            @QueryParam("estado") String estadoTexto) throws Exception {
        exigirAdministrador();
        EstadoAuditoria estado = convertirEstadoOpcional(estadoTexto);
        List<Auditoria> entidades = descripcion == null && estado == null
                ? auditorias.findAll()
                : auditorias.findByFilters(descripcion, estado);
        return convertirAuditorias(entidades);
    }

    @GET
    @Path("{id}")
    public AuditoriaResponseDTO buscarPorId(@PathParam("id") int id)
            throws Exception {
        exigirAdministrador();
        Auditoria auditoria = auditorias.findById(id);
        if (auditoria == null) {
            throw new NotFoundException("No se encontro la auditoria");
        }
        return new AuditoriaResponseDTO(auditoria);
    }

    @POST
    public Response registrar(CrearAuditoriaRequestDTO solicitud)
            throws Exception {
        exigirAdministrador();
        if (solicitud == null) {
            throw new BadRequestException("Indique los datos de la auditoria");
        }
        Auditoria registrada = auditorias.registrar(
                solicitud.getFuncionId(),
                solicitud.getResponsableId(),
                solicitud.getDescripcion());
        return Response.status(Response.Status.CREATED)
                .entity(new AuditoriaResponseDTO(registrada))
                .build();
    }

    @PUT
    @Path("{id}")
    public AuditoriaResponseDTO actualizar(
            @PathParam("id") int id,
            ActualizarAuditoriaRequestDTO solicitud) throws Exception {
        exigirAdministrador();
        if (solicitud == null) {
            throw new BadRequestException("Indique los datos de atencion");
        }
        EstadoAuditoria estado = convertirEstado(solicitud.getEstado());
        Auditoria actualizada = auditorias.actualizarAtencion(
                id,
                solicitud.getResponsableId(),
                estado);
        return new AuditoriaResponseDTO(actualizada);
    }

    @GET
    @Path("opciones/responsables")
    public List<AdministradorResumenDTO> listarResponsables()
            throws Exception {
        exigirAdministrador();
        List<AdministradorResumenDTO> respuesta = new ArrayList<>();
        for (Administrador administrador
                : auditorias.findResponsablesActivos()) {
            respuesta.add(new AdministradorResumenDTO(administrador));
        }
        return respuesta;
    }

    @GET
    @Path("opciones/funciones")
    public List<FuncionResumenDTO> listarFunciones() throws Exception {
        exigirAdministrador();
        List<FuncionResumenDTO> respuesta = new ArrayList<>();
        for (Funcion funcion : auditorias.findFuncionesAsignables()) {
            respuesta.add(new FuncionResumenDTO(funcion));
        }
        return respuesta;
    }

    private List<AuditoriaResponseDTO> convertirAuditorias(
            List<Auditoria> entidades) {
        List<AuditoriaResponseDTO> respuesta = new ArrayList<>();
        for (Auditoria auditoria : entidades) {
            respuesta.add(new AuditoriaResponseDTO(auditoria));
        }
        return respuesta;
    }

    private EstadoAuditoria convertirEstadoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return convertirEstado(valor);
    }

    private EstadoAuditoria convertirEstado(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new BadRequestException("Indique el estado de la auditoria");
        }
        try {
            return EstadoAuditoria.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("El estado de auditoria no es valido");
        }
    }

    private void exigirAdministrador() {
        if (sesion == null || !sesion.isUserInRole("Administrador")) {
            throw new ForbiddenException("Se requiere administrador");
        }
    }
}
