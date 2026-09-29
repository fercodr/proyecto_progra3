package pe.edu.pucp.entrefunciones.dao.espacios;

import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.espacios.Bloqueo;
import java.sql.SQLException;
import java.util.List;

public interface BloqueoDAO extends DAO<Bloqueo, Integer> {
    List<Bloqueo> findBySalaId(int salaId) throws SQLException;
    List<Bloqueo> findByRecursoId(int recursoId) throws SQLException;
}