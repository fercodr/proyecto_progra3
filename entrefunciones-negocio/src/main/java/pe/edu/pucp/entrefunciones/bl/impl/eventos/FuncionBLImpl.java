package pe.edu.pucp.entrefunciones.bl.impl.eventos;

import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.eventos.FuncionBL;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;

import java.sql.SQLException;
import java.util.List;

public class FuncionBLImpl implements FuncionBL {

    private final FuncionDAO funcionDAO = new FuncionDAOImpl();
    private final EventoDAO eventoDAO = new EventoDAOImpl();

    @Override
    public List<Funcion> findAll() throws BLException {
        try {
            return funcionDAO.findAll();

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo listar las funciones", e);
        }
    }

    @Override
    public Funcion findById(Integer id) throws BLException {
        try {
            return funcionDAO.findById(id);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo recuperar la función", e);
        }
    }

    @Override
    public void insert(Funcion funcion) throws BLException {
        validar(funcion);
        validarEvento(funcion);

        try {
            funcionDAO.insert(funcion);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo registrar la función", e);
        }
    }

    @Override
    public void update(Funcion funcion) throws BLException {
        validarExiste(funcion.getId());
        validar(funcion);
        validarEvento(funcion);

        try {
            funcionDAO.update(funcion);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo actualizar la función", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarExiste(id);

        try {
            funcionDAO.delete(id);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo cancelar la función", e);
        }
    }

    private void validar(Funcion funcion) throws BLException {

        if (funcion == null) {
            throw new BLException(
                    "La función no puede ser nula");
        }

        if (funcion.getEvento() == null) {
            throw new BLException(
                    "La función debe pertenecer a un evento");
        }

        if (funcion.getSala() == null) {
            throw new BLException(
                    "La función debe tener una sala");
        }

        if (funcion.getFecha() == null) {
            throw new BLException(
                    "La función debe tener una fecha");
        }

        if (funcion.getHoraInicio() == null) {
            throw new BLException(
                    "La función debe tener una hora de inicio");
        }

        if (funcion.getHoraFin() == null) {
            throw new BLException(
                    "La función debe tener una hora de fin");
        }

        if (!funcion.getHoraFin()
                .isAfter(funcion.getHoraInicio())) {
            throw new BLException(
                    "La hora de fin debe ser posterior a la hora de inicio");
        }

        if (funcion.getIdioma() == null
                || funcion.getIdioma().trim().isEmpty()) {
            throw new BLException(
                    "El idioma de la función no puede estar vacío");
        }

        if (funcion.getEstado() == null) {
            throw new BLException(
                    "La función debe tener un estado");
        }
    }

    private void validarEvento(Funcion funcion)
            throws BLException {

        try {
            if (eventoDAO.findById(
                    funcion.getEvento().getId()) == null) {

                throw new BLException(
                        "No existe el evento asociado a la función");
            }

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo verificar el evento de la función", e);
        }
    }

    private void validarExiste(Integer id)
            throws BLException {

        if (id == null) {
            throw new BLException(
                    "El id de la función no puede ser nulo");
        }

        try {
            if (funcionDAO.findById(id) == null) {
                throw new BLException(
                        "No existe una función con id " + id);
            }

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo verificar la existencia de la función", e);
        }
    }
}