package pe.edu.pucp.entrefunciones.dao.espacios;
import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;

import java.sql.SQLException;
import java.util.List;

public interface SalaDAO  extends DAO<Sala,Integer> {
    List<Sala> findBySedeId(int sedeId) throws SQLException;
}
