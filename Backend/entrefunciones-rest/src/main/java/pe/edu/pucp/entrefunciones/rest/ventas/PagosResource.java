package pe.edu.pucp.entrefunciones.rest.ventas;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.ventas.PagoBL;
import pe.edu.pucp.entrefunciones.bl.impl.ventas.PagoBLImpl;
import pe.edu.pucp.entrefunciones.rest.dto.ventas.PagoDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.ventas.PagoMapper;

import java.util.Map;

@Path("/pagos")
@Produces("application/json")
@Consumes("application/json")
public class PagosResource {
    private final PagoBL pagoBL;

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    public PagosResource() {
        pagoBL = new PagoBLImpl();
    }

    @GET
    public Response findAll() {
        try {
            return Response.ok(PagoMapper.toDTOs(pagoBL.findAll()))
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
            return Response.ok(PagoMapper.toDTO(pagoBL.findById(id)))
                    .build();
        }
        catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @PUT
    @Path("/{id}/validar")
    public Response validar(@PathParam("id") int id) {
        Response prohibido = soloAdministrador();
        if (prohibido != null) {
            return prohibido;
        }
        try {
            pagoBL.validar(id);
            return Response.ok(Map.of("mensaje", "Pago validado y entradas emitidas exitosamente")).build();
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

    @PUT
    @Path("/{id}/rechazar")
    public Response rechazar(@PathParam("id") int id) {
        Response prohibido = soloAdministrador();
        if (prohibido != null) {
            return prohibido;
        }
        try {
            pagoBL.rechazar(id);
            return Response.ok(Map.of("mensaje", "Pago rechazado exitosamente")).build();
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

    private Response soloAdministrador() {
        if (securityContext.isUserInRole("Administrador")) {
            return null;
        }
        return error(Response.Status.FORBIDDEN,
                new Exception("Solo un administrador puede validar o rechazar pagos"));
    }

    private Response error(Response.Status status, Exception ex) {
        return Response.status(status)
                .entity(Map.of("error", ex.getMessage()))
                .build();
    }
}
