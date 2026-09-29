package pe.edu.pucp.negocio.entrefunciones.bl.impl.espacios;
import pe.edu.pucp.negocio.entrefunciones.bl.espacios.ButacaBL;
import pe.edu.pucp.negocio.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.dao.espacios.ButacaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.ButacaDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;


import java.sql.SQLException;
import java.util.List;

public class ButacaBLImpl implements ButacaBL {
    private ButacaDAO butacaDAO = new ButacaDAOImpl();

    @Override
    public List<Butaca> findAll() throws BLException {
        try {
            return butacaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("Error al listar butacas", e);
        }
    }

    @Override
    public List<Butaca> listarPorSala(int idSala) throws BLException {
        try {
            return butacaDAO.findBySalaId(idSala);
        } catch (SQLException e) {
            throw new BLException("Error al listar butacas de la sala", e);
        }
    }

    @Override
    public Butaca findById(Integer id) throws BLException {
        try {
            return butacaDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("Error al buscar butaca", e);
        }
    }

    @Override
    public void insert(Butaca butaca) throws BLException {
        try {
            // RF-M2-03: no repetir fila+columna en la misma sala
            List<Butaca> existentes = butacaDAO.findBySalaId(butaca.getSala().getId());
            boolean repetida = existentes.stream().anyMatch(b ->
                    b.getFila().equalsIgnoreCase(butaca.getFila()) && b.getColumna() == butaca.getColumna());
            if (repetida) {
                throw new BLException("Ya existe una butaca en esa fila y columna para esta sala");
            }
            butacaDAO.insert(butaca);
        } catch (SQLException e) {
            throw new BLException("Error al insertar butaca", e);
        }
    }

    @Override
    public void update(Butaca butaca) throws BLException {
        try {
            butacaDAO.update(butaca);
        } catch (SQLException e) {
            throw new BLException("Error al actualizar butaca", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        try {
            butacaDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("Error al eliminar butaca", e);
        }
    }
}