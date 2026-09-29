package pe.edu.pucp.entrefunciones.bl.impl.catalogo;

import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.catalogo.ObraTeatroBL;
import pe.edu.pucp.entrefunciones.dao.catalogo.ObraTeatroDAO;
import pe.edu.pucp.entrefunciones.dao.impl.catalogo.ObraTeatroDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ObraTeatro;

import java.sql.SQLException;
import java.util.List;

public class ObraTeatroBLImpl implements ObraTeatroBL {

    private final ObraTeatroDAO obraTeatroDAO = new ObraTeatroDAOImpl();

    @Override
    public List<ObraTeatro> findAll() throws BLException {
        try {
            return obraTeatroDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las obras de teatro", e);
        }
    }

    @Override
    public ObraTeatro findById(Integer id) throws BLException {
        try {
            return obraTeatroDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la obra de teatro", e);
        }
    }

    @Override
    public void insert(ObraTeatro obraTeatro) throws BLException {
        validarNombre(obraTeatro);
        try {
            obraTeatroDAO.insert(obraTeatro);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la obra de teatro", e);
        }
    }

    @Override
    public void update(ObraTeatro obraTeatro) throws BLException {
        validarExiste(obraTeatro.getId());
        validarNombre(obraTeatro);
        try {
            obraTeatroDAO.update(obraTeatro);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la obra de teatro", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        try {
            obraTeatroDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar la obra de teatro", e);
        }
    }

    private void validarNombre(ObraTeatro obraTeatro) throws BLException {
        if (obraTeatro.getNombre() == null || obraTeatro.getNombre().trim().isEmpty()) {
            throw new BLException("El nombre de la obra de teatro no puede estar vacio");
        }
    }

    private void validarExiste(int id) throws BLException {
        try {
            if (obraTeatroDAO.findById(id) == null) {
                throw new BLException("No existe una obra de teatro con id " + id);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la obra de teatro", e);
        }
    }
}