package pe.edu.pucp.entrefunciones.dao.impl.ventas;

import pe.edu.pucp.entrefunciones.dao.ventas.ReservaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.usuarios.ClienteDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.ButacaDAOImpl;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;
import pe.edu.pucp.entrefunciones.modelo.ventas.EstadoReserva;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAOImpl implements ReservaDAO {

    @Override
    public List<Reserva> findAll() throws SQLException {
        String sql = "{call listar_reservas()}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql);
                ResultSet rs = cmd.executeQuery()) {

            List<Reserva> reservas = new ArrayList<>();
            while (rs.next()) {
                reservas.add(mapear(rs));
            }
            return reservas;
        }
    }

    @Override
    public List<Reserva> findByClienteId(int clienteId) throws SQLException {
        String sql = "{call listar_reservas_por_cliente(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_cliente_id", clienteId);

            try (ResultSet rs = cmd.executeQuery()) {
                List<Reserva> reservas = new ArrayList<>();
                while (rs.next()) {
                    reservas.add(mapear(rs));
                }
                return reservas;
            }
        }
    }

    @Override
    public List<Reserva> findByFuncionId(int funcionId) throws SQLException {
        String sql = "{call listar_reservas_por_funcion(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_funcion_id", funcionId);

            try (ResultSet rs = cmd.executeQuery()) {
                List<Reserva> reservas = new ArrayList<>();
                while (rs.next()) {
                    reservas.add(mapear(rs));
                }
                return reservas;
            }
        }
    }

    @Override
    public Reserva findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_reserva_por_id(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insert(Reserva reserva) throws SQLException {
        try (Connection conn = DBManager.getInstance().getConnection()) {
            insertConButacas(reserva, conn);
        }
    }

    @Override
    public void insertConButacas(Reserva reserva, Connection conn) throws SQLException {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }

        // 1. Insertar la cabecera "reserva"
        String sqlReserva = "{call insertar_reserva(?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sqlReserva)) {
            cmd.setInt("p_funcion_id", reserva.getFuncion().getId());
            cmd.setInt("p_cliente_id", reserva.getCliente().getId());
            cmd.setTimestamp("p_fecha_creacion", Timestamp.valueOf(reserva.getFechaCreacion()));
            cmd.setTimestamp("p_fecha_expiracion", Timestamp.valueOf(reserva.getFechaExpiracion()));
            cmd.setString("p_estado", reserva.getEstado().name());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la reserva");
            }
            reserva.setId(cmd.getInt("p_id"));
        }
        // busca insertar una fila en reserva_butaca POR CADA butaca elegida.
        String sqlDetalle = "{call insertar_reserva_butaca(?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sqlDetalle)) {
            for (Butaca butaca : reserva.getButacas()) {
                cmd.setInt("p_reserva_id", reserva.getId());
                cmd.setInt("p_butaca_id", butaca.getId());
                if (cmd.executeUpdate() == 0) {
                    throw new SQLException("No se pudo asociar la butaca  a la reserva");
                }
            }
        }
    }

    @Override
    public void update(Reserva reserva) throws SQLException {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }

        String sql = "{call modificar_reserva(?, ?, ?, ?, ?, ?, ?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", reserva.getId());
            cmd.setInt("p_funcion_id", reserva.getFuncion().getId());
            cmd.setInt("p_cliente_id", reserva.getCliente().getId());
            cmd.setTimestamp("p_fecha_creacion", Timestamp.valueOf(reserva.getFechaCreacion()));
            cmd.setTimestamp("p_fecha_expiracion", Timestamp.valueOf(reserva.getFechaExpiracion()));
            cmd.setString("p_estado", reserva.getEstado().name());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la reserva");
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call eliminar_reserva(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar la reserva");
            }
        }
    }

    private Reserva mapear(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");

        ArrayList<Butaca> butacas = new ArrayList<>( new ButacaDAOImpl().findByReservaId(id));

        Reserva reserva = new Reserva(id, new FuncionDAOImpl().findById(rs.getInt("funcion_id")),
                new ClienteDAOImpl().findById(rs.getInt("cliente_id")), butacas,
                rs.getTimestamp("fecha_creacion").toLocalDateTime(),
                rs.getTimestamp("fecha_expiracion").toLocalDateTime()
        );

        reserva.setEstado(Enum.valueOf(EstadoReserva.class, rs.getString("estado")));
        return reserva;
    }
}