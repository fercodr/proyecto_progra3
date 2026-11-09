package pe.edu.pucp.entrefunciones.rest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
@Provider public class ErroresRest implements ExceptionMapper<Exception> {
    private static final java.util.logging.Logger LOG=java.util.logging.Logger.getLogger(ErroresRest.class.getName());
    @Override public Response toResponse(Exception e) {
        int status=500;String mensaje="No se pudo completar la operación";
        if(e instanceof WebApplicationException web){status=web.getResponse().getStatus();mensaje=e.getMessage();}
        else if(e instanceof BLException){status=409;mensaje=e.getMessage();}
        else if(e instanceof IllegalArgumentException || e instanceof jakarta.json.JsonException){status=400;mensaje="Revise los datos enviados";}
        if(status==500)LOG.log(java.util.logging.Level.SEVERE,"Error REST",e);
        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(java.util.Map.of("mensaje",mensaje)).build();
    }
}
