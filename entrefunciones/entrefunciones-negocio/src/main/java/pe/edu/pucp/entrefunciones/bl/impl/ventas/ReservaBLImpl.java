package pe.edu.pucp.entrefunciones.bl.impl.ventas;

import pe.edu.pucp.entrefunciones.bl.ventas.ReservaBL;
import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.impl.TransaccionBL;
import pe.edu.pucp.entrefunciones.dao.ventas.ReservaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.ventas.ReservaDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;

import java.sql.SQLException;
import java.util.List;

/**
 * Antes, crearReserva() manejaba su propia transaccion "a mano" (abria una
 * Connection con DBManager, hacia setAutoCommit(false)/commit()/rollback()
 * por su cuenta), en paralelo y sin ninguna relacion con TransactionsManager
 * — el mismo patron ad-hoc que tenia Sede/Bloqueo antes de migrar a
 * TransaccionBL. Se reemplaza aqui por el mismo patron que ya usa el resto
 * del proyecto: la BL es quien inicia/confirma/revierte (via
 * TransaccionBL.ejecutar) y el DAO participa con
 * TransactionsManager.getConnection().
 */
public class ReservaBLImpl implements ReservaBL {
    private final ReservaDAO reservaDAO;

    public ReservaBLImpl() {
        this(new ReservaDAOImpl());
    }

    public ReservaBLImpl(ReservaDAO reservaDAO) {
        if (reservaDAO == null) {
            throw new IllegalArgumentException("El reservaDAO no puede ser nulo");
        }
        this.reservaDAO = reservaDAO;
    }

    @Override
    public List<Reserva> findAll() throws BLException {
        try {
            return reservaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("Error al listar reservas", e);
        }
    }

    @Override
    public List<Reserva> listarPorCliente(int idCliente) throws BLException {
        try {
            return reservaDAO.findByClienteId(idCliente);
        } catch (SQLException e) {
            throw new BLException("Error al listar reservas del cliente", e);
        }
    }

    @Override
    public List<Reserva> listarPorFuncion(int idFuncion) throws BLException {
        try {
            return reservaDAO.findByFuncionId(idFuncion);
        } catch (SQLException e) {
            throw new BLException("Error al listar reservas de la función", e);
        }
    }

    @Override
    public Reserva findById(Integer id) throws BLException {
        try {
            return reservaDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("Error al buscar reserva", e);
        }
    }

    @Override
    public void insert(Reserva reserva) throws BLException {
        crearReserva(reserva); // delega al método con transacción
    }

    @Override
    public void crearReserva(Reserva reserva) throws BLException {
        if (reserva == null) {
            throw new BLException("La reserva no puede ser nula");
        }
        // El propio constructor de Reserva ya exige butacas no vacias en
        // asiento numerado o cantidad > 0 en aforo general, asi que si el
        // objeto llego hasta aqui ya es valido en cualquiera de los dos casos.

        TransaccionBL.ejecutar("Error al crear la reserva", () -> {
            reservaDAO.insertConButacas(reserva);
        });
    }

    @Override
    public void update(Reserva reserva) throws BLException {
        if (reserva == null) {
            throw new BLException("La reserva no puede ser nula");
        }
        TransaccionBL.ejecutar("Error al actualizar reserva", () -> reservaDAO.update(reserva));
    }

    @Override
    public void delete(Integer id) throws BLException {
        if (id == null) {
            throw new BLException("El id de la reserva no puede ser nulo");
        }
        TransaccionBL.ejecutar("Error al eliminar reserva", () -> reservaDAO.delete(id));
    }
}
