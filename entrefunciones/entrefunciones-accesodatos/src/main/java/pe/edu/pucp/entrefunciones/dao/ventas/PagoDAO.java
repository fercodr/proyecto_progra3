package pe.edu.pucp.entrefunciones.dao.ventas;
import java.sql.SQLException;
import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.ventas.Pago;

/**
 * DAO de Pago (entidad de una sola tabla, sin IEliminable: su ciclo de
 * vida se expresa con estado, ver seccion 8.3 del informe). Ademas del
 * CRUD generico, agrega las dos busquedas que exige el negocio: por la
 * reserva a la que pertenece (uk_pago_reserva: a lo mas un pago por
 * reserva) y por su codigo de pago (uk_pago_codigo, el que el cliente usa
 * para registrar el pago en RF-M5-02).
 */
public interface PagoDAO extends DAO<Pago, Integer> {
    Pago findByReserva(int reservaId) throws SQLException;
    Pago findByCodigoPago(String codigoPago) throws SQLException;
}
