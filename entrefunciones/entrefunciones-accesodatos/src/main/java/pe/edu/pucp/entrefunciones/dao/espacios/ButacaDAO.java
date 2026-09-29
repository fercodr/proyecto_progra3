package pe.edu.pucp.entrefunciones.dao.espacios;

import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import java.sql.SQLException;
import java.util.List;

public interface ButacaDAO extends DAO<Butaca, Integer> {
    List<Butaca> findBySalaId(int salaId) throws SQLException;
    List<Butaca> findByReservaId(int reservaId) throws SQLException;
}
