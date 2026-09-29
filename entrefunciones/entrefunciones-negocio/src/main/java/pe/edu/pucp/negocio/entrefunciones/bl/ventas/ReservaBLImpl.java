package pe.edu.pucp.entrefunciones.bl.impl.ventas;

import pe.edu.pucp.entrefunciones.bl.ventas.ReservaBL;
import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.dao.ventas.ReservaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.ventas.ReservaDAOImpl;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ReservaBLImpl implements ReservaBL {
    private ReservaDAO reservaDAO = new ReservaDAOImpl();

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
        crearReserva(reserva);   // delega al método con transacción
    }

    @Override
    public void crearReserva(Reserva reserva) throws BLException {
        if (reserva.getButacas() == null || reserva.getButacas().isEmpty()) {
            throw new BLException("Debe seleccionar al menos una butaca para reservar");
        }

        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            reservaDAO.insertConButacas(reserva, conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new BLException("Error al revertir la reserva", rollbackEx);
                }
            }
            throw new BLException("Error al crear la reserva: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    @Override
    public void update(Reserva reserva) throws BLException {
        try {
            reservaDAO.update(reserva);
        } catch (SQLException e) {
            throw new BLException("Error al actualizar reserva", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        try {
            reservaDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("Error al eliminar reserva", e);
        }
    }
}
