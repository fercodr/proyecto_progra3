package pe.edu.pucp.entrefunciones.dao.ventas;

import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ReservaDAO extends DAO<Reserva, Integer> {
    List<Reserva> findByClienteId(int clienteId) throws SQLException;
    List<Reserva> findByFuncionId(int funcionId) throws SQLException;
    void insertConButacas(Reserva reserva, Connection conn) throws SQLException;
}