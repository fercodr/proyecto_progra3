package pe.edu.pucp.entrefunciones.bl.impl.espacios;

import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.espacios.SedeBL;
import pe.edu.pucp.entrefunciones.dao.espacios.SedeDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SedeDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.sql.SQLException;
import java.util.List;

public class SedeBLImpl implements SedeBL {
    private final SedeDAO sedeDAO;

    public SedeBLImpl() {
        this(new SedeDAOImpl());
    }

    public SedeBLImpl(SedeDAO sedeDAO) {
        if (sedeDAO == null) {
            throw new IllegalArgumentException("El DAO de sede no puede ser nulo");
        }
        this.sedeDAO = sedeDAO;
    }

    @Override
    public List<Sede> findAll() throws BLException {
        try {
            return sedeDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar las sedes", e);
        }
    }

    @Override
    public Sede findById(Integer id) throws BLException {
        validarId(id, "sede");
        try {
            return sedeDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la sede", e);
        }
    }

    @Override
    public void insert(Sede sede) throws BLException {
        validarDatos(sede);
        try {
            sedeDAO.insert(sede);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la sede", e);
        }
    }

    @Override
    public void update(Sede sede) throws BLException {
        validarDatos(sede);
        validarId(sede.getId(), "sede");
        validarExiste(sede.getId());
        try {
            sedeDAO.update(sede);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la sede", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarId(id, "sede");
        Sede existente = validarExiste(id);
        if (!existente.estaActivo()) {
            return;
        }
        try {
            sedeDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo desactivar la sede", e);
        }
    }

    private Sede validarExiste(Integer id) throws BLException {
        try {
            Sede sede = sedeDAO.findById(id);
            if (sede == null) {
                throw new BLException("No existe una sede con id " + id);
            }
            return sede;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la sede", e);
        }
    }

    private void validarDatos(Sede sede) throws BLException {
        if (sede == null) {
            throw new BLException("La sede no puede ser nula");
        }
        validarTexto(sede.getRazonSocial(), 150, "La razón social");
        validarTexto(sede.getDireccion(), 200, "La dirección");
        validarTexto(sede.getCiudad(), 100, "La ciudad");
        if (sede.getTelefonoContacto() != null && sede.getTelefonoContacto().length() > 20) {
            throw new BLException("El teléfono de contacto no puede exceder 20 caracteres");
        }
    }

    private void validarTexto(String valor, int longitudMaxima, String campo) throws BLException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new BLException(campo + " no puede estar vacío");
        }
        if (valor.length() > longitudMaxima) {
            throw new BLException(campo + " no puede exceder " + longitudMaxima + " caracteres");
        }
    }

    private void validarId(Integer id, String entidad) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id de la " + entidad + " debe ser positivo");
        }
    }
}
