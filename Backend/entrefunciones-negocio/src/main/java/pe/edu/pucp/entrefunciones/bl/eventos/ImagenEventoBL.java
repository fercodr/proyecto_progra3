package pe.edu.pucp.entrefunciones.bl.eventos;

import pe.edu.pucp.entrefunciones.bl.error.BLException;

public interface ImagenEventoBL {

    // Sube el póster del evento a S3, guarda su URL en evento.imagen_url y la devuelve.
    String subirPoster(int eventoId, String tipoContenido, byte[] contenido) throws BLException;
}
