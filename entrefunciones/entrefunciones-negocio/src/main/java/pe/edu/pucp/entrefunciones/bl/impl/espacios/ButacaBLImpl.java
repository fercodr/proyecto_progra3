package pe.edu.pucp.entrefunciones.bl.impl.espacios;

import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.espacios.ButacaBL;
import pe.edu.pucp.entrefunciones.dao.espacios.ButacaDAO;
import pe.edu.pucp.entrefunciones.dao.espacios.SalaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.ButacaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;

import java.sql.SQLException;
import java.util.List;

public class ButacaBLImpl implements ButacaBL {
    private final ButacaDAO butacaDAO;
    private final SalaDAO salaDAO;

    public ButacaBLImpl() {
        this(new ButacaDAOImpl(), new SalaDAOImpl());
    }

    public ButacaBLImpl(ButacaDAO butacaDAO, SalaDAO salaDAO) {
        if (butacaDAO == null || salaDAO == null) {
            throw new IllegalArgumentException("Los DAO de butaca no pueden ser nulos");
        }
        this.butacaDAO = butacaDAO;
        this.salaDAO = salaDAO;
    }

    @Override
    public List<Butaca> findAll() throws BLException {
        try {
            return butacaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar las butacas", e);
        }
    }

    @Override
    public List<Butaca> listarPorSala(int idSala) throws BLException {
        validarId(idSala, "sala");
        validarExisteSala(idSala);
        try {
            return butacaDAO.findBySalaId(idSala);
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar las butacas de la sala", e);
        }
    }

    @Override
    public List<Butaca> listarPorReserva(int idReserva) throws BLException {
        validarId(idReserva, "reserva");
        try {
            return butacaDAO.findByReservaId(idReserva);
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar las butacas de la reserva", e);
        }
    }

    @Override
    public Butaca findById(Integer id) throws BLException {
        validarId(id, "butaca");
        try {
            return butacaDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la butaca", e);
        }
    }

    @Override
    public void insert(Butaca butaca) throws BLException {
        validarDatos(butaca);
        Sala sala = validarExisteSala(butaca.getSala().getId());
        if (!sala.estaActivo()) {
            throw new BLException("No se puede registrar una butaca en una sala inactiva");
        }

        Butaca mismaPosicion = buscarMismaPosicion(butaca, null);
        if (mismaPosicion != null) {
            if (mismaPosicion.estaActivo()) {
                throw new BLException("Ya existe una butaca en esa fila y columna de la sala");
            }
            butaca.setId(mismaPosicion.getId());
            validarAforo(butaca, mismaPosicion.getId(), sala);
            try {
                butacaDAO.update(butaca);
                return;
            } catch (SQLException e) {
                throw new BLException("No se pudo reactivar la butaca existente", e);
            }
        }

        validarAforo(butaca, null, sala);
        try {
            butacaDAO.insert(butaca);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la butaca", e);
        }
    }

    @Override
    public void update(Butaca butaca) throws BLException {
        validarDatos(butaca);
        validarId(butaca.getId(), "butaca");
        validarExisteButaca(butaca.getId());
        Sala sala = validarExisteSala(butaca.getSala().getId());
        if (butaca.estaActivo() && !sala.estaActivo()) {
            throw new BLException("No se puede activar o trasladar una butaca a una sala inactiva");
        }
        if (buscarMismaPosicion(butaca, butaca.getId()) != null) {
            throw new BLException("Ya existe otra butaca en esa fila y columna de la sala");
        }
        validarAforo(butaca, butaca.getId(), sala);
        try {
            butacaDAO.update(butaca);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la butaca", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarId(id, "butaca");
        Butaca existente = validarExisteButaca(id);
        if (!existente.estaActivo()) {
            return;
        }
        try {
            butacaDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo desactivar la butaca", e);
        }
    }

    private Butaca validarExisteButaca(Integer id) throws BLException {
        try {
            Butaca butaca = butacaDAO.findById(id);
            if (butaca == null) {
                throw new BLException("No existe una butaca con id " + id);
            }
            return butaca;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la butaca", e);
        }
    }

    private Sala validarExisteSala(Integer id) throws BLException {
        try {
            Sala sala = salaDAO.findById(id);
            if (sala == null) {
                throw new BLException("No existe la sala indicada");
            }
            return sala;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la sala de la butaca", e);
        }
    }

    private Butaca buscarMismaPosicion(Butaca butaca, Integer idExcluir) throws BLException {
        try {
            return butacaDAO.findBySalaId(butaca.getSala().getId()).stream()
                    .filter(registrada -> idExcluir == null || registrada.getId() != idExcluir)
                    .filter(registrada -> registrada.getFila().trim()
                            .equalsIgnoreCase(butaca.getFila().trim()))
                    .filter(registrada -> registrada.getColumna() == butaca.getColumna())
                    .findFirst()
                    .orElse(null);
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la posición de la butaca", e);
        }
    }

    private void validarAforo(Butaca butaca, Integer idExcluir, Sala sala) throws BLException {
        if (!butaca.estaActivo()) {
            return;
        }
        try {
            long activasSinActual = butacaDAO.findBySalaId(sala.getId()).stream()
                    .filter(Butaca::estaActivo)
                    .filter(registrada -> idExcluir == null || registrada.getId() != idExcluir)
                    .count();
            if (activasSinActual + 1 > sala.getAforo()) {
                throw new BLException("La cantidad de butacas activas no puede superar el aforo de la sala");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el aforo de la sala", e);
        }
    }

    private void validarDatos(Butaca butaca) throws BLException {
        if (butaca == null) {
            throw new BLException("La butaca no puede ser nula");
        }
        if (butaca.getSala() == null) {
            throw new BLException("La butaca debe pertenecer a una sala");
        }
        validarId(butaca.getSala().getId(), "sala");
        if (butaca.getFila() == null || butaca.getFila().trim().isEmpty()) {
            throw new BLException("La fila de la butaca no puede estar vacía");
        }
        if (butaca.getFila().length() > 5) {
            throw new BLException("La fila de la butaca no puede exceder 5 caracteres");
        }
        if (butaca.getColumna() < 0) {
            throw new BLException("La columna de la butaca no puede ser negativa");
        }
        if (butaca.getTipoButaca() == null || butaca.getTipoButaca().trim().isEmpty()) {
            throw new BLException("El tipo de butaca no puede estar vacío");
        }
        if (butaca.getTipoButaca().length() > 30) {
            throw new BLException("El tipo de butaca no puede exceder 30 caracteres");
        }
    }

    private void validarId(Integer id, String entidad) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id de " + entidad + " debe ser positivo");
        }
    }
}
