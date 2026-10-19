package pe.edu.pucp.entrefunciones.dao.ventas;

import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;
import java.sql.SQLException;
import java.util.List;

public interface ReservaDAO extends DAO<Reserva, Integer> {
    List<Reserva> findByClienteId(int clienteId) throws SQLException;
    List<Reserva> findByFuncionId(int funcionId) throws SQLException;

    /**
     * Inserta la cabecera de la reserva y su detalle de butacas dentro de
     * la transaccion activa en el hilo (ver TransactionsManager). Ya no
     * recibe una Connection por parametro: antes se recibia una conexion
     * cruda desde ReservaBLImpl, lo que duplicaba el manejo de
     * transacciones fuera de TransactionsManager y rompia el patron que ya
     * usa el resto del proyecto (Sede/Bloqueo/Butaca). El llamador debe
     * haber iniciado una transaccion antes de invocar este metodo (por
     * ejemplo con TransaccionBL.ejecutar en la capa de negocio).
     */
    void insertConButacas(Reserva reserva) throws SQLException;
}
