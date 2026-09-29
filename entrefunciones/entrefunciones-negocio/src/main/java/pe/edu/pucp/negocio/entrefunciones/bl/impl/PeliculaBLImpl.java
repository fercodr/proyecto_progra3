package pe.edu.pucp.negocio.entrefunciones.bl.impl;

import pe.edu.pucp.negocio.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.PeliculaBL;
import pe.edu.pucp.entrefunciones.dao.catalogo.PeliculaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.catalogo.PeliculaDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Pelicula;

import java.sql.SQLException;
import java.util.List;

public class PeliculaBLImpl implements PeliculaBL {

    private final PeliculaDAO peliculaDAO = new PeliculaDAOImpl();

    @Override
    public List<Pelicula> findAll() throws BLException {
        try {
            return peliculaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las peliculas", e);
        }
    }

    @Override
    public Pelicula findById(Integer id) throws BLException {
        try {
            return peliculaDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la pelicula", e);
        }
    }

    @Override
    public void insert(Pelicula pelicula) throws BLException {
        validarNombre(pelicula);
        try {
            peliculaDAO.insert(pelicula);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la pelicula", e);
        }
    }

    @Override
    public void update(Pelicula pelicula) throws BLException {
        validarExiste(pelicula.getId());
        validarNombre(pelicula);
        try {
            peliculaDAO.update(pelicula);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la pelicula", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        try {
            peliculaDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar la pelicula", e);
        }
    }

    private void validarNombre(Pelicula pelicula) throws BLException {
        if (pelicula.getNombre() == null || pelicula.getNombre().trim().isEmpty()) {
            throw new BLException("El nombre de la pelicula no puede estar vacio");
        }
    }

    private void validarExiste(int id) throws BLException {
        try {
            if (peliculaDAO.findById(id) == null) {
                throw new BLException("No existe una pelicula con id " + id);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la pelicula", e);
        }
    }
}