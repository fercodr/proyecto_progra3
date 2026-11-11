package pe.edu.pucp.entrefunciones.bl.impl.eventos;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.eventos.FuncionBL;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAOImpl;
import pe.edu.pucp.entrefunciones.dao.espacios.SalaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.espacios.SedeDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SedeDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class FuncionBLImpl implements FuncionBL {

    private final FuncionDAO funcionDAO = new FuncionDAOImpl();
    private final EventoDAO eventoDAO = new EventoDAOImpl();
    private final SalaDAO salaDAO = new SalaDAOImpl();
    private final SedeDAO sedeDAO = new SedeDAOImpl();
    private final pe.edu.pucp.entrefunciones.dao.impl.ControlOperacionesDAO control = new pe.edu.pucp.entrefunciones.dao.impl.ControlOperacionesDAO();
    private void validarDisponibilidad(Funcion funcion) throws SQLException, BLException {
        control.bloquearSala(funcion.getSala().getId());
        var sala = new pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl().findById(funcion.getSala().getId());
        if(sala==null || !sala.estaActivo() || !sala.getSede().estaActivo()) throw new BLException("La sala o sede no está activa");
        if(funcion.getEstado()!=pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion.CANCELADA)
            for(Funcion otra:funcionDAO.findBySalaId(sala.getId()))
                if(otra.getId()!=funcion.getId() && otra.getEstado()!=pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion.CANCELADA
                    && otra.getFecha().equals(funcion.getFecha())
                    && funcion.getHoraInicio().isBefore(otra.getHoraFin()) && funcion.getHoraFin().isAfter(otra.getHoraInicio()))
                    throw new BLException("El horario se cruza con otra función de la misma sala");
    }

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
    public List<Funcion> findReportes(LocalDate desde, LocalDate hasta,
                                      Integer idSede, Integer idSala)
            throws BLException {
        if (desde == null || hasta == null) {
            throw new ValidacionException("Las fechas desde y hasta son obligatorias");
        }
        if (desde.isAfter(hasta)) {
            throw new ValidacionException("La fecha desde no puede ser posterior a hasta");
        }
        if (idSede != null && idSede <= 0) {
            throw new ValidacionException("El identificador de sede debe ser positivo");
        }
        if (idSala != null && idSala <= 0) {
            throw new ValidacionException("El identificador de sala debe ser positivo");
        }

        try {
            if (idSede != null && sedeDAO.findById(idSede) == null) {
                throw new ValidacionException("No existe la sede indicada");
            }
            if (idSala != null) {
                var sala = salaDAO.findById(idSala);
                if (sala == null) {
                    throw new ValidacionException("No existe la sala indicada");
                }
                if (idSede != null && sala.getSede().getId() != idSede) {
                    throw new ValidacionException("La sala no pertenece a la sede indicada");
                }
            }
            return funcionDAO.findReportes(desde, hasta, idSede, idSala);
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar el reporte de funciones", e);
        }
    }

    @Override public void insert(Funcion funcion) throws BLException {
        validar(funcion);
        pe.edu.pucp.entrefunciones.bl.Transacciones.ejecutar("No se pudo programar la función",()-> {
            validarEvento(funcion);validarDisponibilidad(funcion);funcionDAO.insert(funcion);return null;
        });
    }
    @Override public void update(Funcion funcion) throws BLException {
        validar(funcion);
        pe.edu.pucp.entrefunciones.bl.Transacciones.ejecutar("No se pudo actualizar la función",()-> {
            validarExiste(funcion.getId());validarEvento(funcion);validarDisponibilidad(funcion);
            funcionDAO.update(funcion);return null;
        });
    }
    @Override public void delete(Integer id) throws BLException {
        pe.edu.pucp.entrefunciones.bl.Transacciones.ejecutar("No se pudo cancelar la función",()-> {
            validarExiste(id);funcionDAO.delete(id);return null;
        });
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
                    "La hora de fin debe ser posterior al inicio en el mismo día; divida los horarios que cruzan medianoche");
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
            var evento = eventoDAO.findById(funcion.getEvento().getId());
            if (evento == null || !evento.estaActivo()) {

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
