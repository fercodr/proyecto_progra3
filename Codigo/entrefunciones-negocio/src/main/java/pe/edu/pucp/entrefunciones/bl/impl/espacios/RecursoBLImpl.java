package pe.edu.pucp.entrefunciones.bl.impl.espacios;

import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.espacios.RecursoBL;
import pe.edu.pucp.entrefunciones.dao.espacios.RecursoDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.RecursoDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.Recurso;

import java.sql.SQLException;
import java.util.List;

public class RecursoBLImpl implements RecursoBL {
    private final RecursoDAO recursoDAO;

    public RecursoBLImpl() {
        this(new RecursoDAOImpl());
    }

    public RecursoBLImpl(RecursoDAO recursoDAO) {
        if (recursoDAO == null) {
            throw new IllegalArgumentException("El DAO de recurso no puede ser nulo");
        }
        this.recursoDAO = recursoDAO;
    }

    @Override
    public List<Recurso> findAll() throws BLException {
        try {
            return recursoDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar los recursos", e);
        }
    }

    @Override
    public Recurso findById(Integer id) throws BLException {
        validarId(id);
        try {
            return recursoDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el recurso", e);
        }
    }

    @Override
    public void insert(Recurso recurso) throws BLException {
        validarDatos(recurso);
        TransaccionBL.ejecutar("No se pudo registrar el recurso",
                () -> recursoDAO.insert(recurso));
    }

    @Override
    public void update(Recurso recurso) throws BLException {
        validarDatos(recurso);
        validarId(recurso.getId());
        TransaccionBL.ejecutar("No se pudo actualizar el recurso", () -> {
            validarExiste(recurso.getId());
            recursoDAO.update(recurso);
        });
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarId(id);
        TransaccionBL.ejecutar("No se pudo desactivar el recurso", () -> {
            Recurso existente = validarExiste(id);
            if (existente.estaActivo()) {
                recursoDAO.delete(id);
            }
        });
    }

    private Recurso validarExiste(Integer id) throws BLException {
        try {
            Recurso recurso = recursoDAO.findById(id);
            if (recurso == null) {
                throw new BLException("No existe un recurso con id " + id);
            }
            return recurso;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia del recurso", e);
        }
    }

    private void validarDatos(Recurso recurso) throws BLException {
        if (recurso == null) {
            throw new BLException("El recurso no puede ser nulo");
        }
        validarTexto(recurso.getNombre(), 100, "El nombre del recurso");
        validarTexto(recurso.getTipo(), 50, "El tipo del recurso");
    }

    private void validarTexto(String valor, int longitudMaxima, String campo) throws BLException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new BLException(campo + " no puede estar vacío");
        }
        if (valor.length() > longitudMaxima) {
            throw new BLException(campo + " no puede exceder " + longitudMaxima + " caracteres");
        }
    }

    private void validarId(Integer id) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id del recurso debe ser positivo");
        }
    }
}
