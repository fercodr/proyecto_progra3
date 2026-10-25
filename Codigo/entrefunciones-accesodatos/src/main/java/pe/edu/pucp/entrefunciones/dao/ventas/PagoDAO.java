package pe.edu.pucp.entrefunciones.dao.ventas;
import java.sql.SQLException;
import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.ventas.Pago;


public interface PagoDAO extends DAO<Pago, Integer> {
    Pago findByReserva(int reservaId) throws SQLException;
    Pago findByCodigoPago(String codigoPago) throws SQLException;
}
