package pe.edu.pucp.entrefunciones.bl.impl.espacios;

import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.espacios.SalaBL;
import pe.edu.pucp.entrefunciones.dao.espacios.ButacaDAO;
import pe.edu.pucp.entrefunciones.dao.espacios.SalaDAO;
import pe.edu.pucp.entrefunciones.dao.espacios.SedeDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.ButacaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SedeDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.sql.SQLException;
import java.util.List;

public class SalaBLImpl implements SalaBL {
    private final SalaDAO salaDAO;
    private final SedeDAO sedeDAO;
    private final ButacaDAO butacaDAO;

    public SalaBLImpl() {
        this(new SalaDAOImpl(), new SedeDAOImpl(), new ButacaDAOImpl());
    }

    public SalaBLImpl(SalaDAO salaDAO, SedeDAO sedeDAO, ButacaDAO butacaDAO) {
        if (salaDAO == null || sedeDAO == null || butacaDAO == null) {
            throw new IllegalArgumentException("Los DAO de sala no pueden ser nulos");
        }
        this.salaDAO = salaDAO;
        this.sedeDAO = sedeDAO;
        this.butacaDAO = butacaDAO;
    }

    @Override
    public List<Sala> findAll() throws BLException {
        try {
            return salaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar las salas", e);
        }
    }

    @Override
    public List<Sala> listarPorSede(int idSede) throws BLException {
        validarId(idSede, "sede");
        validarExisteSede(idSede);
        try {
            return salaDAO.findBySedeId(idSede);
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar las salas de la sede", e);
        }
    }

    @Override
    public Sala findById(Integer id) throws BLException {
        validarId(id, "sala");
        try {
            return salaDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la sala", e);
        }
    }

    @Override
    public void insert(Sala sala) throws BLException {
        validarDatos(sala);
        TransaccionBL.ejecutar("No se pudo registrar la sala", () -> {
            Sede sedeRelacionada = obtenerSede(sala);
            Sede sede = validarExisteSede(sedeRelacionada.getId());
            if (!sede.estaActivo()) {
                throw new BLException("No se puede registrar una sala en una sede inactiva");
            }
            validarNombreDisponible(sala, null);
            salaDAO.insert(sala);
        });
    }

    @Override
    public void update(Sala sala) throws BLException {
        validarDatos(sala);
        validarId(sala.getId(), "sala");
        TransaccionBL.ejecutar("No se pudo actualizar la sala", () -> {
            Sala existente = validarExisteSala(sala.getId());
            Sede sedeRelacionada = obtenerSede(sala);
            int sedeOriginalId = existente.getSede().getId();
            int sedeNuevaId = sedeRelacionada.getId();
            if (sedeOriginalId != sedeNuevaId) {
                throw new BLException("No se puede trasladar una sala a otra sede");
            }
            Sede sede = validarExisteSede(sedeNuevaId);
            if (sala.estaActivo() && !sede.estaActivo()) {
                throw new BLException("No se puede activar una sala de una sede inactiva");
            }
            validarNombreDisponible(sala, sala.getId());
            validarAforo(sala);
            salaDAO.update(sala);
        });
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarId(id, "sala");
        TransaccionBL.ejecutar("No se pudo desactivar la sala", () -> {
            Sala existente = validarExisteSala(id);
            if (existente.estaActivo()) {
                salaDAO.delete(id);
            }
        });
    }

    private Sala validarExisteSala(Integer id) throws BLException {
        try {
            Sala sala = salaDAO.findById(id);
            if (sala == null) {
                throw new BLException("No existe una sala con id " + id);
            }
            return sala;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la sala", e);
        }
    }

    private Sede validarExisteSede(Integer id) throws BLException {
        try {
            Sede sede = sedeDAO.findById(id);
            if (sede == null) {
                throw new BLException("No existe la sede indicada");
            }
            return sede;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la sede de la sala", e);
        }
    }

    private void validarNombreDisponible(Sala sala, Integer idExcluir) throws BLException {
        try {
            for (Sala registrada : salaDAO.findBySedeId(sala.getSede().getId())) {
                boolean esLaMisma = idExcluir != null && registrada.getId() == idExcluir;
                if (!esLaMisma && registrada.getNombre().trim()
                        .equalsIgnoreCase(sala.getNombre().trim())) {
                    throw new BLException("Ya existe una sala con ese nombre en la sede");
                }
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el nombre de la sala", e);
        }
    }

    private void validarAforo(Sala sala) throws BLException {
        try {
            long butacasActivas = butacaDAO.findBySalaId(sala.getId()).stream()
                    .filter(Butaca::estaActivo)
                    .count();
            if (butacasActivas > sala.getAforo()) {
                throw new BLException(
                        "El aforo no puede ser menor que la cantidad de butacas activas");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el aforo de la sala", e);
        }
    }

    private void validarDatos(Sala sala) throws BLException {
        if (sala == null) {
            throw new BLException("La sala no puede ser nula");
        }
        Sede sede = obtenerSede(sala);
        validarId(sede.getId(), "sede");
        validarTexto(sala.getNombre(), 100, "El nombre de la sala");
        validarTexto(sala.getTipoSala(), 50, "El tipo de sala");
        if (sala.getAforo() <= 0) {
            throw new BLException("El aforo debe ser mayor que cero");
        }
    }

    private Sede obtenerSede(Sala sala) throws BLException {
        try {
            Sede sede = sala.getSede();
            if (sede == null) {
                throw new BLException("La sala debe pertenecer a una sede");
            }
            return sede;
        } catch (IllegalArgumentException e) {
            throw new BLException("La sala debe pertenecer a una sede", e);
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
            throw new BLException("El id de " + entidad + " debe ser positivo");
        }
    }
}
