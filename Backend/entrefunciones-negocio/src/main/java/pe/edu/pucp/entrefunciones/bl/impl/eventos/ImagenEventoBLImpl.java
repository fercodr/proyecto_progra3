package pe.edu.pucp.entrefunciones.bl.impl.eventos;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.eventos.EventoBL;
import pe.edu.pucp.entrefunciones.bl.eventos.ImagenEventoBL;
import pe.edu.pucp.entrefunciones.bl.impl.archivos.S3Almacenamiento;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoImagenDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoImagenDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class ImagenEventoBLImpl implements ImagenEventoBL {

    private static final int TAMANO_MAXIMO = 2 * 1024 * 1024;   // 2 MB, igual que en C#

    // Tipos permitidos -> extensión del archivo en S3
    private static final Map<String, String> TIPOS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final EventoBL eventoBL;
    private final EventoImagenDAO eventoImagenDAO;

    public ImagenEventoBLImpl() {
        eventoBL = new EventoBLImpl();
        eventoImagenDAO = new EventoImagenDAOImpl();
    }

    @Override
    public String subirPoster(int eventoId, String tipoContenido, byte[] contenido) throws BLException {
        // 1) Validaciones
        if (contenido == null || contenido.length == 0) {
            throw new ValidacionException("La imagen está vacía");
        }
        if (contenido.length > TAMANO_MAXIMO) {
            throw new ValidacionException("La imagen no puede pesar más de 2 MB");
        }
        String extension = TIPOS.get(tipoContenido == null ? "" : tipoContenido.toLowerCase());
        if (extension == null) {
            throw new ValidacionException("Solo se permiten imágenes JPG, PNG o WEBP");
        }

        Evento evento = eventoBL.findById(eventoId);
        if (evento == null) {
            throw new NoEncontradoException("No existe el evento con id " + eventoId);
        }

        // 2) Subir a S3 con un nombre único (si se sube otra, no pisa la anterior en caché)
        String clave = "eventos/" + eventoId + "/" + UUID.randomUUID() + extension;
        String url = new S3Almacenamiento().subir(clave, contenido, tipoContenido);

        // 3) Guardar la URL en la BD
        try {
            eventoImagenDAO.actualizarImagen(evento, url);
        } catch (SQLException e) {
            throw new BLException("La imagen se subió, pero no se pudo guardar en el evento", e);
        }
        return url;
    }
}
