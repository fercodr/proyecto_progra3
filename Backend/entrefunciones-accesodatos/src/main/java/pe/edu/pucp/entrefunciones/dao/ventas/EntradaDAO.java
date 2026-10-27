package pe.edu.pucp.entrefunciones.dao.ventas;
import java.sql.SQLException;
import java.util.List;
import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;


public interface EntradaDAO extends DAO<Entrada, Integer> {
    List<Entrada> findByReserva(int reservaId) throws SQLException;
    Entrada findByCodigoQR(String codigoQR) throws SQLException;
}
