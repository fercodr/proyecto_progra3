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
import pe.edu.pucp.entrefunciones.dao.impl.ventas.EntradaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.ventas.ReservaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.ButacaDAOImpl;
import pe.edu.pucp.entrefunciones.rest.dto.ventas.EntradaDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.ventas.EntradaMapper;
import pe.edu.pucp.entrefunciones.rest.AutorizacionRest;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;

import java.util.List;
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
        entradaBL = new EntradaBLImpl(new EntradaDAOImpl(new ReservaDAOImpl(), new ButacaDAOImpl()));
    }

    // GET /entradas | /entradas?idReserva=5
    @GET
    public Response findAll(@QueryParam("idReserva") Integer idReserva) {
        try {
            if (idReserva != null) {
                List<Entrada> entradas = entradaBL.buscarPorReserva(idReserva);
                if (entradas.isEmpty()) {
                    exigirReservaPropia(idReserva);
                } else {
                    AutorizacionRest.exigirPropietarioOAdministrador(
                            securityContext,
                            entradas.get(0).getReserva().getCliente().getId());
                }
                return Response.ok(EntradaMapper.toDTOs(entradas))
                        .build();
            }
            AutorizacionRest.exigirAdministrador(securityContext);
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
            Entrada entrada = entradaBL.findById(id);
            exigirEntradaPropia(entrada);
            return Response.ok(EntradaMapper.toDTO(entrada))
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
        AutorizacionRest.exigirAdministrador(securityContext);
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
            exigirEntradaPropia(entradaBL.findById(id));
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

    private void exigirReservaPropia(int reservaId) throws BLException {
        Reserva reserva;
        try {
            reserva = new ReservaDAOImpl().findById(reservaId);
        } catch (java.sql.SQLException ex) {
            throw new BLException("No se pudo verificar la reserva", ex);
        }
        if (reserva == null) {
            throw new NoEncontradoException("No existe la reserva indicada");
        }
        AutorizacionRest.exigirPropietarioOAdministrador(
                securityContext, reserva.getCliente().getId());
    }

    private void exigirEntradaPropia(Entrada entrada) {
        AutorizacionRest.exigirPropietarioOAdministrador(
                securityContext, entrada.getReserva().getCliente().getId());
    }

    private Response error(Response.Status status, Exception ex) {
        return Response.status(status)
                .entity(Map.of("error", ex.getMessage()))
                .build();
    }
}
