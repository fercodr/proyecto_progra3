package pe.edu.pucp.entrefunciones.rest.auditoria;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
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
import java.util.List;
import java.util.Map;

import pe.edu.pucp.entrefunciones.bl.auditoria.AuditoriaBL;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.impl.auditoria.AuditoriaBLImpl;
import pe.edu.pucp.entrefunciones.modelo.auditoria.Auditoria;
import pe.edu.pucp.entrefunciones.modelo.auditoria.EstadoAuditoria;
import pe.edu.pucp.entrefunciones.rest.dto.auditoria.AuditoriaDTO;
import pe.edu.pucp.entrefunciones.rest.dto.catalogo.FuncionDTO;
import pe.edu.pucp.entrefunciones.rest.dto.usuarios.AdministradorDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.auditoria.AuditoriaMapper;
import pe.edu.pucp.entrefunciones.rest.mapper.catalogo.FuncionMapper;
import pe.edu.pucp.entrefunciones.rest.mapper.usuarios.AdministradorMapper;

@Path("auditorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditoriaResource {

    @Context
    private SecurityContext sesion;

    private final AuditoriaBL auditorias = new AuditoriaBLImpl();

    @GET
    //FindAll QueryParam-----OK
    public Response listar(@QueryParam("descripcion") String descripcion, @QueryParam("estado") String estadoTexto) {
        exigirAdministrador();
        try {
            List<AuditoriaDTO> dtos = (descripcion == null && estadoTexto == null)
                    ? AuditoriaMapper.toDTOs(auditorias.findAll())
                    : AuditoriaMapper.toDTOs(
                            auditorias.findByFilters(
                                    descripcion,
                                    convertirEstadoOpcional(estadoTexto)));
            return Response.ok(dtos).build();
        } catch (BadRequestException ex) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        } catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        } catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    "No se pudo consultar las auditorias");
        }

    }

    //findById----CORRECT
    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") int id) {
        exigirAdministrador();
        try{
            return Response.ok(AuditoriaMapper.toDTO(auditorias.findById(id)))
                    .build();
        }catch(NoEncontradoException e){
            return error(Response.Status.NOT_FOUND, e.getMessage());
        }catch (ValidacionException e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        }catch (BLException e){
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    "No se pudo recuperar la auditoria");
        }
    }


    @POST
    public Response insert(AuditoriaDTO solicitud){
        exigirAdministrador();
        if (solicitud == null) return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("mensaje", "entidad no especificada"))
                .build();
        try{
            validarRelacionesInsert(solicitud);
            Auditoria registradaModelo = auditorias.registrar(
                    solicitud.getFuncion().getId(),
                    solicitud.getResponsable().getId(),
                    solicitud.getDescripcion());
            AuditoriaDTO registrada = AuditoriaMapper.toDTO(registradaModelo);

            return Response.status(Response.Status.CREATED)
                    .entity(registrada)
                    .build();
        }catch (ValidacionException | IllegalArgumentException e){
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        }catch (NoEncontradoException e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        }catch (BLException e){
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    "No se pudo registrar la auditoria");
        }
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") int id,AuditoriaDTO solicitud) {
        exigirAdministrador();
        try {
            if (solicitud == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("mensaje", "entidad no especificada"))
                        .build();
            }
            if (id != solicitud.getId()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("mensaje", "El id y el payload no coinciden"))
                        .build();
            }
            validarRelacionesUpdate(solicitud);
            if (solicitud.getEstado() == null) {
                throw new ValidacionException("El estado es obligatorio");
            }
            Auditoria registradaModelo = auditorias.actualizarAtencion(
                    id,
                    solicitud.getResponsable().getId(),
                    solicitud.getDescripcion(),
                    convertirEstado(solicitud.getEstado()));
            AuditoriaDTO registrada = AuditoriaMapper.toDTO(registradaModelo);

            return Response.ok(registrada).build();
        }
        catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex.getMessage());
        } catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        } catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    "No se pudo actualizar la auditoria");
        }
    }

    @GET
    @Path("opciones/responsables")
    public Response listarResponsables(){
        exigirAdministrador();
        try{
            List<AdministradorDTO> dtos = AdministradorMapper.toDTOs(
                    auditorias.findResponsablesActivos());
            return Response.ok(dtos).build();
        }catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    "No se pudieron listar los responsables");
        }

    }

    @GET
    @Path("opciones/funciones")
    public Response listarFunciones() {
        exigirAdministrador();
        try {
            List<FuncionDTO> dtos = FuncionMapper.toDTOs(
                    auditorias.findFuncionesAsignables());
            return Response.ok(dtos).build();
        }catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    "No se pudieron listar las funciones");
        }
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

    private void validarRelacionesInsert(AuditoriaDTO solicitud)
            throws ValidacionException {
        if (solicitud.getFuncion() == null
                || solicitud.getFuncion().getId() <= 0) {
            throw new ValidacionException("La funcion es obligatoria");
        }
        validarResponsable(solicitud);
    }

    private void validarRelacionesUpdate(AuditoriaDTO solicitud)
            throws ValidacionException {
        validarResponsable(solicitud);
    }

    private void validarResponsable(AuditoriaDTO solicitud)
            throws ValidacionException {
        if (solicitud.getResponsable() == null
                || solicitud.getResponsable().getId() <= 0) {
            throw new ValidacionException("El responsable es obligatorio");
        }
    }

    private void exigirAdministrador() {
        if (sesion == null || !sesion.isUserInRole("Administrador")) {
            throw new ForbiddenException("Se requiere administrador");
        }
    }
    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado)
                .entity(Map.of("mensaje", mensaje))
                .build();
    }
}
