package pe.edu.pucp.entrefunciones.rest.auditoria;

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
import java.util.List;
import java.util.Map;

import pe.edu.pucp.entrefunciones.bl.auditoria.AuditoriaBL;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.eventos.FuncionBL;
import pe.edu.pucp.entrefunciones.bl.impl.auditoria.AuditoriaBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.eventos.FuncionBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.usuarios.AdministradorBLImpl;
import pe.edu.pucp.entrefunciones.bl.usuarios.AdministradorBL;
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
    private final FuncionBL funcionBL = new FuncionBLImpl();
    private final AdministradorBL administradorBL = new AdministradorBLImpl();

    @GET
    //FindAll QueryParam-----OK
    public Response listar(@QueryParam("descripcion") String descripcion, @QueryParam("estado") String estadoTexto) throws Exception {
        exigirAdministrador();
        try{
            List<AuditoriaDTO> dtos =(descripcion == null && estadoTexto == null)?AuditoriaMapper.toDTOs(auditorias.findAll()):
            AuditoriaMapper.toDTOs(auditorias.findByFilters(descripcion, convertirEstadoOpcional(estadoTexto)));
            return Response.ok(dtos).build();
        }catch(Exception ex){
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }

    }

    //findById----CORRECT
    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") int id)throws Exception {
        exigirAdministrador();
        try{
            return Response.ok(AuditoriaMapper.toDTO(auditorias.findById(id)))
                    .build();
        }catch(NotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error",e.getMessage()))
                    .build();
        }catch (BLException e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error",e.getMessage()))
                    .build();
        }
    }


    @POST
    public Response insert(AuditoriaDTO solicitud){
        exigirAdministrador();
        if (solicitud == null) return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "entidad no especificada"))
                .build();
        try{
            Auditoria auditoria = AuditoriaMapper.toModel(solicitud);

            auditorias.insert(auditoria);

            AuditoriaDTO registrada = AuditoriaMapper.toDTO(
                    auditorias.findById(auditoria.getId()));

            return Response.status(Response.Status.CREATED)
                    .entity(registrada)
                    .build();
        }catch (Exception e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") int id,AuditoriaDTO solicitud) {
        exigirAdministrador();
        try {
            if (id != solicitud.getId()) {
                throw new BLException("El id y el payload no coinciden");
            }
            auditorias.update(AuditoriaMapper.toModel(solicitud));
            AuditoriaDTO registrada = AuditoriaMapper.toDTO(auditorias.findById(solicitud.getId()));

            return Response.ok(registrada).build();
        }
        catch (ValidacionException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        } catch (BLException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("opciones/responsables")
    public Response listarResponsables(){
        exigirAdministrador();
        try{
            List<AdministradorDTO> dtos = AdministradorMapper.toDTOs(administradorBL.findAll());
            return Response.ok(dtos).build();
        }catch (BLException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
        }

    }

    @GET
    @Path("opciones/funciones")
    public Response listarFunciones() {
        exigirFuncion();
        try {
            List<FuncionDTO> dtos = FuncionMapper.toDTOs(funcionBL.findAll());
            return Response.ok(dtos).build();
        }catch (BLException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ex.getMessage()))
                    .build();
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

    private void exigirAdministrador() {
        if (sesion == null || !sesion.isUserInRole("Administrador")) {
            throw new ForbiddenException("Se requiere administrador");
        }
    }
    private void exigirFuncion() {
        if (sesion == null || !sesion.isUserInRole("Administrador")) {
            throw new ForbiddenException("Se requiere administrador");
        }
    }
}
