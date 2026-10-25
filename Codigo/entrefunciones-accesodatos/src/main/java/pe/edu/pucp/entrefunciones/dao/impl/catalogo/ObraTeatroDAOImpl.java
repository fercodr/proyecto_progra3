package pe.edu.pucp.entrefunciones.dao.impl.catalogo;

import pe.edu.pucp.entrefunciones.dao.catalogo.ObraTeatroDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ObraTeatro;

import java.sql.SQLException;
import java.util.List;

public class ObraTeatroDAOImpl implements ObraTeatroDAO {
    private final EventoDAO eventoDAO;

    public ObraTeatroDAOImpl() {
        this(new EventoDAOImpl());
    }

    ObraTeatroDAOImpl(EventoDAO eventoDAO) {
        this.eventoDAO = eventoDAO;
    }

    @Override
    public List<ObraTeatro> findAll() throws SQLException {
        return eventoDAO.findAll().stream()
                .filter(ObraTeatro.class::isInstance)
                .map(ObraTeatro.class::cast)
                .toList();
    }

    @Override
    public ObraTeatro findById(Integer id) throws SQLException {
        Evento evento = eventoDAO.findById(id);
        if (evento == null) {
            return null;
        }
        if (!(evento instanceof ObraTeatro obra)) {
            throw new SQLException("El evento " + id + " no es una obra de teatro");
        }
        return obra;
    }

    @Override
    public void insert(ObraTeatro obraTeatro) throws SQLException {
        eventoDAO.insert(obraTeatro);
    }

    @Override
    public void update(ObraTeatro obraTeatro) throws SQLException {
        eventoDAO.update(obraTeatro);
    }

    @Override
    public void delete(Integer id) throws SQLException {
        eventoDAO.delete(id);
    }
}
