package pe.edu.pucp.entrefunciones.dao.impl.catalogo;

import pe.edu.pucp.entrefunciones.dao.catalogo.PeliculaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Pelicula;

import java.sql.SQLException;
import java.util.List;

public class PeliculaDAOImpl implements PeliculaDAO {
    private final EventoDAO eventoDAO;

    public PeliculaDAOImpl() {
        this(new EventoDAOImpl());
    }

    PeliculaDAOImpl(EventoDAO eventoDAO) {
        this.eventoDAO = eventoDAO;
    }

    @Override
    public List<Pelicula> findAll() throws SQLException {
        return eventoDAO.findAll().stream()
                .filter(Pelicula.class::isInstance)
                .map(Pelicula.class::cast)
                .toList();
    }

    @Override
    public Pelicula findById(Integer id) throws SQLException {
        Evento evento = eventoDAO.findById(id);
        if (evento == null) {
            return null;
        }
        if (!(evento instanceof Pelicula pelicula)) {
            throw new SQLException("El evento " + id + " no es una pelicula");
        }
        return pelicula;
    }

    @Override
    public void insert(Pelicula pelicula) throws SQLException {
        eventoDAO.insert(pelicula);
    }

    @Override
    public void update(Pelicula pelicula) throws SQLException {
        eventoDAO.update(pelicula);
    }

    @Override
    public void delete(Integer id) throws SQLException {
        eventoDAO.delete(id);
    }
}
