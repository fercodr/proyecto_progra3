package pe.edu.pucp.entrefunciones.dao.espacios;

import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public interface SedeDAO extends DAO<Sede,Integer> {
    Sede findByIdCompleto(Integer id) throws SQLException;
    List<Sede> findByIds(Collection<Integer> ids) throws SQLException;
}
