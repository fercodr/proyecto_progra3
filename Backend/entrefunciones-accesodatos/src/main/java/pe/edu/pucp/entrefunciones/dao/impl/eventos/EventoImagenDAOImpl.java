package pe.edu.pucp.entrefunciones.dao.impl.eventos;

import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;

import java.sql.CallableStatement;
import java.sql.SQLException;

public class EventoImagenDAOImpl implements EventoImagenDAO {

    /**
     * Usa el procedimiento que ya existe, modificar_evento, que solo actualiza la tabla
     * evento (precio, activo e imagen_url). Se mantienen el precio y el estado actuales.
     * No se usa EventoDAOImpl.update porque ese también reescribe las funciones del evento.
     */
    @Override
    public void actualizarImagen(Evento evento, String imagenUrl) throws SQLException {
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo");
        }

        String sql = "{call modificar_evento(?, ?, ?, ?)}";

        try (
            ConnectionScope scope = ConnectionScope.openForRead();
            CallableStatement cmd = scope.connection().prepareCall(sql)
        ) {
            cmd.setInt("p_id", evento.getId());
            cmd.setDouble("p_precio", evento.getPrecio());
            cmd.setBoolean("p_activo", evento.estaActivo());
            cmd.setString("p_imagen_url", imagenUrl);
            cmd.executeUpdate();
        }
    }
}
