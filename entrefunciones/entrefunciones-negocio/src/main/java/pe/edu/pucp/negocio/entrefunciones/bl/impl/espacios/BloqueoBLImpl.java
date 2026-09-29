package pe.edu.pucp.entrefunciones.bl.impl.espacios;

import pe.edu.pucp.entrefunciones.bl.espacios.BloqueoBL;
import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.dao.espacios.BloqueoDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.BloqueoDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.Bloqueo;

import java.sql.SQLException;
import java.util.List;

public class BloqueoBLImpl implements BloqueoBL {
    private BloqueoDAO bloqueoDAO = new BloqueoDAOImpl();

    @Override
    public List<Bloqueo> findAll() throws BLException {
        try {
            return bloqueoDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("Error al listar bloqueos", e);
        }
    }

    @Override
    public List<Bloqueo> listarPorSala(int idSala) throws BLException {
        try {
            return bloqueoDAO.findBySalaId(idSala);
        } catch (SQLException e) {
            throw new BLException("Error al listar bloqueos de la sala", e);
        }
    }

    @Override
    public List<Bloqueo> listarPorRecurso(int idRecurso) throws BLException {
        try {
            return bloqueoDAO.findByRecursoId(idRecurso);
        } catch (SQLException e) {
            throw new BLException("Error al listar bloqueos del recurso", e);
        }
    }

    @Override
    public Bloqueo findById(Integer id) throws BLException {
        try {
            return bloqueoDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("Error al buscar bloqueo", e);
        }
    }

    @Override
    public void insert(Bloqueo bloqueo) throws BLException {
        try {
            bloqueoDAO.insert(bloqueo);
        } catch (SQLException e) {
            throw new BLException("Error al insertar bloqueo", e);
        }
    }

    @Override
    public void update(Bloqueo bloqueo) throws BLException {
        try {
            bloqueoDAO.update(bloqueo);
        } catch (SQLException e) {
            throw new BLException("Error al actualizar bloqueo", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        try {
            bloqueoDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("Error al eliminar bloqueo", e);
        }
    }
}
