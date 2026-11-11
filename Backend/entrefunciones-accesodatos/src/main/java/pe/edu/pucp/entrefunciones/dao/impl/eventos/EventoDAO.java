package pe.edu.pucp.entrefunciones.dao.impl.eventos;
import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
public interface EventoDAO extends DAO<Evento, Integer> {
    List<Evento> findByIds(Collection<Integer> ids) throws SQLException;
}
