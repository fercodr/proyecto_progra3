package pe.edu.pucp.entrefunciones.dao.ventas;
import java.sql.SQLException;
import java.util.List;
import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;

/**
 * DAO de Entrada (una sola tabla, sin IEliminable: su ciclo de vida se
 * expresa con estado, igual que Pago). Una Reserva puede tener varias
 * entradas (una por butaca reservada), por eso findByReserva devuelve
 * una lista y no un solo objeto. findByCodigoQR es la busqueda que
 * exige RF-M6-01 (validar el ingreso del asistente a partir del codigo
 * QR).
 */
public interface EntradaDAO extends DAO<Entrada, Integer> {
    List<Entrada> findByReserva(int reservaId) throws SQLException;
    Entrada findByCodigoQR(String codigoQR) throws SQLException;
}
