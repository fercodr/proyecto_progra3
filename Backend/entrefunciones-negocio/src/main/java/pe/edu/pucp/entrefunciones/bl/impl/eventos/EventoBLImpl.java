package pe.edu.pucp.entrefunciones.bl.impl.eventos;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.eventos.EventoBL;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class EventoBLImpl implements EventoBL {

    private final EventoDAO eventoDAO;

    public EventoBLImpl() {
        this(new EventoDAOImpl());
    }

    EventoBLImpl(EventoDAO eventoDAO) {
        this.eventoDAO = Objects.requireNonNull(eventoDAO);
    }

    @Override
    public List<Evento> findAll() throws BLException {
        try {
            return eventoDAO.findAll();

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo listar los eventos", e);
        }
    }

    @Override
    public Evento findById(Integer id) throws BLException {
        try {
            return eventoDAO.findById(id);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo recuperar el evento", e);
        }
    }

    @Override
    public void insert(Evento evento) throws BLException {
        validar(evento);

        try {
            prepararFunciones(evento);

            // EventoDAOImpl inserta:
            // 1. Evento
            // 2. Subtipo
            // 3. Funciones
            // Todo dentro de una transacción.
            eventoDAO.insert(evento);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo registrar el evento", e);
        }
    }

    @Override
    public void update(Evento evento) throws BLException {
        validarExiste(evento.getId());
        validar(evento);

        try {
            prepararFunciones(evento);

            // EventoDAOImpl también actualiza
            // las funciones que componen el evento.
            eventoDAO.update(evento);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo actualizar el evento", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarExiste(id);

        try {
            // EventoDAOImpl realiza el eliminado lógico
            // del evento y cancela sus funciones.
            eventoDAO.delete(id);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo eliminar el evento", e);
        }
    }

    private void validar(Evento evento) throws BLException {

        if (evento == null) {
            throw new BLException(
                    "El evento no puede ser nulo");
        }

        if (evento.getPrecio() < 0) {
            throw new BLException(
                    "El precio del evento no puede ser negativo");
        }

        if (evento.getTitulo() == null
                || evento.getTitulo().trim().isEmpty()) {
            throw new BLException(
                    "El evento debe tener un título");
        }

        if (evento.getFunciones() == null) {
            throw new BLException(
                    "La lista de funciones del evento no puede ser nula");
        }
    }

    private void prepararFunciones(Evento evento)
            throws BLException {

        for (Funcion funcion : evento.getFunciones()) {

            if (funcion == null) {
                throw new BLException(
                        "El evento contiene una función nula");
            }

            // Mantiene correctamente la composición.
            funcion.setEvento(evento);

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
                    "El idioma de la función no puede estar vacío");
        }

        if (funcion.getEstado() == null) {
            throw new BLException(
                    "La función debe tener un estado");
        }
    }

    private void validarExiste(Integer id)
            throws BLException {

        if (id == null) {
            throw new BLException(
                    "El id del evento no puede ser nulo");
        }

        try {
            if (eventoDAO.findById(id) == null) {
                throw new BLException(
                        "No existe un evento con id " + id);
            }

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo verificar la existencia del evento", e);
        }
    }
}
