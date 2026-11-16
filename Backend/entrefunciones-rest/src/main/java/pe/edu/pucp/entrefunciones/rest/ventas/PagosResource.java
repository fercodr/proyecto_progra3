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
import pe.edu.pucp.entrefunciones.modelo.ventas.Pago;
import pe.edu.pucp.entrefunciones.bl.impl.ventas.PagoBLImpl;
import pe.edu.pucp.entrefunciones.rest.dto.ventas.PagoDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.ventas.PagoMapper;
import pe.edu.pucp.entrefunciones.rest.AutorizacionRest;

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
        AutorizacionRest.exigirAdministrador(securityContext);
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
            Pago pago = pagoBL.findById(id);
            exigirPagoPropio(pago);
            return Response.ok(PagoMapper.toDTO(pago))
                    .build();
        }
        catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @POST
    @Path("/{id}/registrar")
    public Response registrar(@PathParam("id") int id) {
        try {
            Pago pago = pagoBL.findById(id);
            exigirPagoPropio(pago);
            pagoBL.procesarPago(id, pago.getMonto());
            return Response.ok(Map.of("mensaje", "Pago registrado correctamente")).build();
        }
        catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    @POST
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

    @POST
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
        if (AutorizacionRest.esAdministrador(securityContext)) {
            return null;
        }
        return error(Response.Status.FORBIDDEN,
                new Exception("Solo un administrador puede validar o rechazar pagos"));
    }

    @GET
    @Path("/codigo/{codigoPago}")
    public Response findByCodigo(@PathParam("codigoPago") String codigoPago) {
        AutorizacionRest.exigirAdministrador(securityContext);
        try {
            Pago pago = pagoBL.buscarPorCodigo(codigoPago);
            if (pago == null) {
                return error(Response.Status.NOT_FOUND,
                        new NoEncontradoException("No existe un pago con el código indicado"));
            }
            return Response.ok(PagoMapper.toDTO(pago)).build();
        }
        catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    private void exigirPagoPropio(Pago pago) {
        AutorizacionRest.exigirPropietarioOAdministrador(
                securityContext, pago.getReserva().getCliente().getId());
    }

    private Response error(Response.Status status, Exception ex) {
        return Response.status(status)
                .entity(Map.of("error", ex.getMessage()))
                .build();
    }
}
