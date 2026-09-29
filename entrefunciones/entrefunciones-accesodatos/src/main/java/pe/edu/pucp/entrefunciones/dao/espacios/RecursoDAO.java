package pe.edu.pucp.entrefunciones.dao.espacios;
import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.espacios.Recurso;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RecursoDAO extends DAO<Recurso,Integer> {
    private Recurso mapear(ResultSet rs, Recurso recurso)throws SQLException;
}
