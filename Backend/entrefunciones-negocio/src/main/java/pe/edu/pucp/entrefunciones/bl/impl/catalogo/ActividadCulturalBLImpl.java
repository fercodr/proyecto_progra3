package pe.edu.pucp.entrefunciones.bl.impl.catalogo;

import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.catalogo.ActividadCulturalBL;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ActividadCultural;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActividadCulturalBLImpl implements ActividadCulturalBL {

    private final EventoDAO eventoDAO = new EventoDAOImpl();

    @Override
    public List<ActividadCultural> findAll() throws BLException {
        try {
            List<Evento> eventos = eventoDAO.findAll();
            List<ActividadCultural> actividades = new ArrayList<>();

            for (Evento evento : eventos) {
                if (evento instanceof ActividadCultural actividad) {
                    actividades.add(actividad);
                }
            }

            return actividades;

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo listar las actividades culturales", e);
        }
    }

    @Override
    public ActividadCultural findById(Integer id) throws BLException {
        try {
            Evento evento = eventoDAO.findById(id);

            if (evento == null) {
                return null;
            }

            if (!(evento instanceof ActividadCultural)) {
                throw new BLException(
                        "El evento con id " + id
                                + " no es una actividad cultural");
            }

            return (ActividadCultural) evento;

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo recuperar la actividad cultural", e);
        }
    }

    @Override
    public void insert(ActividadCultural actividad) throws BLException {
        validar(actividad);

        try {
            prepararFunciones(actividad);
            eventoDAO.insert(actividad);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo registrar la actividad cultural", e);
        }
    }

    @Override
    public void update(ActividadCultural actividad) throws BLException {
        validarExiste(actividad.getId());
        validar(actividad);

        try {
            prepararFunciones(actividad);
            eventoDAO.update(actividad);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo actualizar la actividad cultural", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarExiste(id);

        try {
            eventoDAO.delete(id);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo eliminar la actividad cultural", e);
        }
    }

    private void validar(ActividadCultural actividad)
            throws BLException {

        if (actividad == null) {
            throw new BLException(
                    "La actividad cultural no puede ser nula");
        }

        if (actividad.getNombre() == null
                || actividad.getNombre().trim().isEmpty()) {
            throw new BLException(
                    "El nombre de la actividad cultural no puede estar vacío");
        }

        if (actividad.getTipoActividad() == null) {
            throw new BLException(
                    "La actividad cultural debe tener un tipo");
        }

        if (actividad.getPrecio() < 0) {
            throw new BLException(
                    "El precio no puede ser negativo");
        }
    }

    private void prepararFunciones(ActividadCultural actividad)
            throws BLException {

        if (actividad.getFunciones() == null) {
            throw new BLException(
                    "La lista de funciones no puede ser nula");
        }

        for (Funcion funcion : actividad.getFunciones()) {

            if (funcion == null) {
                throw new BLException(
                        "La actividad contiene una función nula");
            }

            funcion.setEvento(actividad);

            validarFuncion(funcion);
        }
    }

    private void validarFuncion(Funcion funcion)
            throws BLException {

        if (funcion.getSala() == null) {
            throw new BLException(
                    "La función debe tener una sala");
        }

        if (funcion.getFecha() == null) {
            throw new BLException(
                    "La función debe tener una fecha");
        }

        if (funcion.getHoraInicio() == null
                || funcion.getHoraFin() == null) {
            throw new BLException(
                    "La función debe tener hora de inicio y fin");
        }

        if (!funcion.getHoraFin()
                .isAfter(funcion.getHoraInicio())) {
            throw new BLException(
                    "La hora de fin debe ser posterior a la hora de inicio");
        }

        if (funcion.getIdioma() == null
                || funcion.getIdioma().trim().isEmpty()) {
            throw new BLException(
                    "La función debe tener un idioma");
        }
    }

    private void validarExiste(Integer id) throws BLException {

        if (id == null) {
            throw new BLException(
                    "El id de la actividad cultural no puede ser nulo");
        }

        try {
            Evento evento = eventoDAO.findById(id);

            if (evento == null) {
                throw new BLException(
                        "No existe una actividad cultural con id " + id);
            }

            if (!(evento instanceof ActividadCultural)) {
                throw new BLException(
                        "El evento con id " + id
                                + " no es una actividad cultural");
            }

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo verificar la existencia "
                            + "de la actividad cultural", e);
        }
    }
}