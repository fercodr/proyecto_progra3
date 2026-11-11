package pe.edu.pucp.entrefunciones.rest.ventas;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.ventas.EntradaBL;
import pe.edu.pucp.entrefunciones.bl.impl.ventas.EntradaBLImpl;
import pe.edu.pucp.entrefunciones.rest.dto.ventas.EntradaDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.ventas.EntradaMapper;

import java.util.Map;

@Path("/entradas")
@Produces("application/json")
@Consumes("application/json")
public class EntradasResource {
    private final EntradaBL entradaBL;

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    public EntradasResource() {
        entradaBL = new EntradaBLImpl();
    }

    // GET /entradas | /entradas?idReserva=5
    @GET
    public Response findAll(@QueryParam("idReserva") Integer idReserva) {
        try {
            if (idReserva != null) {
                return Response.ok(EntradaMapper.toDTOs(entradaBL.buscarPorReserva(idReserva)))
                        .build();
            }
            return Response.ok(EntradaMapper.toDTOs(entradaBL.findAll()))
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
            return Response.ok(EntradaMapper.toDTO(entradaBL.findById(id)))
                    .build();
        }
        catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex);
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    // RF-M6-01: Validar el ingreso del asistente mediante código QR en puerta
    @PUT
    @Path("/validar-ingreso")
    public Response validarIngreso(Map<String, String> payload) {
        try {
            String codigoQR = payload != null ? payload.get("codigoQR") : null;
            if (codigoQR == null || codigoQR.isEmpty()) {
                throw new ValidacionException("Debe proporcionar el código QR para validar el ingreso");
            }
            entradaBL.validarIngreso(codigoQR);
            return Response.ok(Map.of("mensaje", "Ingreso validado exitosamente")).build();
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

    // RF-M5-03: Eliminado lógico de la entrada (cambio de estado a cancelada)
    @PUT
    @Path("/{id}/cancelar")
    public Response cancelar(@PathParam("id") int id) {
        try {
            entradaBL.cancelar(id);
            return Response.ok(Map.of("mensaje", "Entrada cancelada exitosamente")).build();
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

    private Response error(Response.Status status, Exception ex) {
        return Response.status(status)
                .entity(Map.of("error", ex.getMessage()))
                .build();
    }
}
