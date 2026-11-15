package pe.edu.pucp.entrefunciones.dao.impl.eventos;

import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;

import java.sql.SQLException;

// Solo cambia la imagen del evento (no toca sus funciones ni sus datos de subtipo).
public interface EventoImagenDAO {

    void actualizarImagen(Evento evento, String imagenUrl) throws SQLException;
}
