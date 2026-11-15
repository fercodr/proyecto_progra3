package pe.edu.pucp.entrefunciones.rest.imagenes;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.eventos.ImagenEventoBL;
import pe.edu.pucp.entrefunciones.bl.impl.eventos.ImagenEventoBLImpl;
import pe.edu.pucp.entrefunciones.rest.dto.imagenes.ImagenDTO;

import java.util.Base64;
import java.util.Map;

/**
 * Póster de un evento (se guarda en Amazon S3):
 *   POST /eventos/{id}/imagen   body: {"tipoContenido":"image/jpeg","contenidoBase64":"..."}
 *        -> 200 {"imagenUrl": "https://..."} | 400 | 404 | 500
 */
@Path("/eventos/{id}/imagen")
@Produces("application/json")
@Consumes("application/json")
public class ImagenesEventoResource {

    private final ImagenEventoBL imagenEventoBL;

    public ImagenesEventoResource() {
        imagenEventoBL = new ImagenEventoBLImpl();
    }

    @POST
    public Response subir(@PathParam("id") int id, ImagenDTO dto) {
        try {
            if (dto == null || dto.getContenidoBase64() == null) {
                throw new ValidacionException("Envíe la imagen");
            }
            byte[] contenido = Base64.getDecoder().decode(dto.getContenidoBase64());

            String url = imagenEventoBL.subirPoster(id, dto.getTipoContenido(), contenido);
            return Response.ok(Map.of("imagenUrl", url)).build();
        }
        catch (IllegalArgumentException ex) {
            // base64 mal formado
            return error(Response.Status.BAD_REQUEST, "La imagen no tiene un formato válido");
        }
        catch (ValidacionException ex) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        }
        catch (NoEncontradoException ex) {
            return error(Response.Status.NOT_FOUND, ex.getMessage());
        }
        catch (BLException ex) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private Response error(Response.Status status, String mensaje) {
        return Response.status(status)
                .entity(Map.of("mensaje", mensaje))
                .build();
    }
}
