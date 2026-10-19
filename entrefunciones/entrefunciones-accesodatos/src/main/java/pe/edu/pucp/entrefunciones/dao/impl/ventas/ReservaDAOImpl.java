package pe.edu.pucp.entrefunciones.dao.impl.ventas;

import pe.edu.pucp.entrefunciones.dao.ventas.ReservaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.usuarios.ClienteDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.ButacaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;
import pe.edu.pucp.entrefunciones.modelo.ventas.EstadoReserva;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Igual que BloqueoDAOImpl/SedeDAOImpl: las lecturas usan
 * ConnectionScope.openForRead() (reutilizan la conexion de la transaccion
 * activa si la hay, o abren y cierran una propia si no) y las escrituras
 * usan TransactionsManager.getConnection() directamente, sin abrir ni
 * cerrar su propia conexion — exigen que el llamador (la capa de negocio)
 * ya haya iniciado una transaccion. Antes, cada metodo abria su propia
 * conexion independiente via DBManager, lo que hacia imposible que
 * insertar_reserva + insertar_reserva_butaca (o, mas grave, el flujo
 * completo de PagoBLImpl.validar: pago + reserva + entradas) fueran
 * atomicos.
 */
public class ReservaDAOImpl implements ReservaDAO {

    @Override
    public List<Reserva> findAll() throws SQLException {
        String sql = "{call listar_reservas()}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
                CallableStatement cmd = scope.connection().prepareCall(sql);
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
        try (ConnectionScope scope = ConnectionScope.openForRead();
                CallableStatement cmd = scope.connection().prepareCall(sql)) {

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
        try (ConnectionScope scope = ConnectionScope.openForRead();
                CallableStatement cmd = scope.connection().prepareCall(sql)) {

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
        try (ConnectionScope scope = ConnectionScope.openForRead();
                CallableStatement cmd = scope.connection().prepareCall(sql)) {

            cmd.setInt("p_id", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insert(Reserva reserva) throws SQLException {
        insertConButacas(reserva);
    }

    @Override
    public void insertConButacas(Reserva reserva) throws SQLException {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }

        Connection conn = TransactionsManager.getConnection();

        // 1. Insertar la cabecera "reserva". p_cantidad solo va con valor en
        // aforo general (ActividadCultural): en asiento numerado debe llegar
        // NULL, que es lo que exige trg_reserva_tipo_y_aforo (la cantidad se
        // deriva de las filas de reserva_butaca, no se duplica en la
        // columna). reserva.esAforoGeneral() es equivalente a
        // butacas.isEmpty() (ver Reserva.java).
        String sqlReserva = "{call insertar_reserva(?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sqlReserva)) {
            cmd.setInt("p_funcion_id", reserva.getFuncion().getId());
            cmd.setInt("p_cliente_id", reserva.getCliente().getId());
            if (reserva.esAforoGeneral()) {
                cmd.setInt("p_cantidad", reserva.getCantidad());
            } else {
                cmd.setNull("p_cantidad", Types.INTEGER);
            }
            cmd.setTimestamp("p_fecha_creacion", Timestamp.valueOf(reserva.getFechaCreacion()));
            cmd.setTimestamp("p_fecha_expiracion", Timestamp.valueOf(reserva.getFechaExpiracion()));
            cmd.setString("p_estado", reserva.getEstado().name());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la reserva");
            }
            reserva.setId(cmd.getInt("p_id"));
        }
        // En aforo general no hay butacas puntuales que asociar: el cupo ya
        // quedo registrado en reserva.cantidad, arriba.
        if (reserva.esAforoGeneral()) {
            return;
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

        Connection conn = TransactionsManager.getConnection();
        String sql = "{call modificar_reserva(?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", reserva.getId());
            cmd.setInt("p_funcion_id", reserva.getFuncion().getId());
            cmd.setInt("p_cliente_id", reserva.getCliente().getId());
            if (reserva.esAforoGeneral()) {
                cmd.setInt("p_cantidad", reserva.getCantidad());
            } else {
                cmd.setNull("p_cantidad", Types.INTEGER);
            }
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

        Connection conn = TransactionsManager.getConnection();
        String sql = "{call eliminar_reserva(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar la reserva");
            }
        }
    }

    /**
     * cantidad viene NULL para una reserva de asiento numerado y con valor
     * para una de aforo general (asi lo exige trg_reserva_tipo_y_aforo, ver
     * decisiones-diseno-aforo-general.md) — es la señal que usa este metodo
     * para decidir con cual de los dos constructores de Reserva reconstruir
     * la fila, sin tener que volver a consultar el tipo de Evento.
     */
    private Reserva mapear(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");

        var funcion = new FuncionDAOImpl().findById(rs.getInt("funcion_id"));
        var cliente = new ClienteDAOImpl().findById(rs.getInt("cliente_id"));
        LocalDateTime fechaCreacion = rs.getTimestamp("fecha_creacion").toLocalDateTime();
        LocalDateTime fechaExpiracion = rs.getTimestamp("fecha_expiracion").toLocalDateTime();

        int cantidad = rs.getInt("cantidad");
        boolean esAforoGeneral = !rs.wasNull();

        Reserva reserva;
        if (esAforoGeneral) {
            reserva = new Reserva(id, funcion, cliente, cantidad, fechaCreacion, fechaExpiracion);
        } else {
            ArrayList<Butaca> butacas = new ArrayList<>(new ButacaDAOImpl().findByReservaId(id));
            reserva = new Reserva(id, funcion, cliente, butacas, fechaCreacion, fechaExpiracion);
        }

        reserva.setEstado(Enum.valueOf(EstadoReserva.class, rs.getString("estado")));
        return reserva;
    }
}
