package pe.edu.pucp.entrefunciones.dao.impl.ventas;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import pe.edu.pucp.entrefunciones.dao.ventas.PagoDAO;
import pe.edu.pucp.entrefunciones.dao.ventas.ReservaDAO;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.ventas.EstadoPago;
import pe.edu.pucp.entrefunciones.modelo.ventas.Pago;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;

/**
 * Implementacion de Pago siguiendo el mismo patron que SedeDAOImpl
 * (CallableStatement con parametros nombrados hacia los procedimientos de
 * sql/entrefunciones-mysql.sql, seccion "pago"). A diferencia de Sede,
 * Pago no tiene IEliminable: su eliminado logico se expresa con estado
 * (ver seccion 8.3 del informe), asi que delete(id) no desactiva un
 * booleano sino que intenta pasar el pago a RECHAZADO — el procedimiento
 * eliminar_pago solo actualiza la fila si todavia esta PENDIENTE (ver
 * comentario alli); si no, executeUpdate() devuelve 0 y este metodo lanza
 * SQLException, para que la capa de negocio decida que hacer.
 *
 * <p>Recibe un ReservaDAO por constructor (en vez de instanciar
 * ReservaDAOImpl directamente) porque esa clase le toca a la parte 2 del
 * equipo y puede no existir todavia al momento de compilar esta parte;
 * cualquier implementacion de ReservaDAO que cumpla el contrato sirve.</p>
 */
public class PagoDAOImpl implements PagoDAO {

    private final ReservaDAO reservaDAO;

    public PagoDAOImpl(ReservaDAO reservaDAO) {
        if (reservaDAO == null) {
            throw new IllegalArgumentException("El reservaDAO no puede ser nulo");
        }
        this.reservaDAO = reservaDAO;
    }

    @Override
    public List<Pago> findAll() throws SQLException {
        String sql = "{call listar_pagos()}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql);
                ResultSet rs = cmd.executeQuery()) {

            List<Pago> pagos = new ArrayList<>();
            while (rs.next()) {
                pagos.add(mapear(rs));
            }
            return pagos;
        }
    }

    @Override
    public Pago findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_pago_por_id(?)}";
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
    public Pago findByReserva(int reservaId) throws SQLException {
        String sql = "{call buscar_pago_por_reserva(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_reserva_id", reservaId);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Pago findByCodigoPago(String codigoPago) throws SQLException {
        if (codigoPago == null || codigoPago.isEmpty()) {
            throw new IllegalArgumentException("El codigoPago no puede ser nulo o vacio");
        }

        String sql = "{call buscar_pago_por_codigo(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setString("p_codigo_pago", codigoPago);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insert(Pago pago) throws SQLException {
        if (pago == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo");
        }

        String sql = "{call insertar_pago(?, ?, ?, ?, ?, ?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_reserva_id", pago.getReserva().getId());
            cmd.setString("p_codigo_pago", pago.getCodigoPago());
            cmd.setDouble("p_monto", pago.getMonto());
            if (pago.getFechaPago() != null) {
                cmd.setTimestamp("p_fecha_pago", Timestamp.valueOf(pago.getFechaPago()));
            } else {
                cmd.setNull("p_fecha_pago", Types.TIMESTAMP);
            }
            cmd.setString("p_estado", pago.getEstado().name());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar el pago");
            }

            pago.setId(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(Pago pago) throws SQLException {
        if (pago == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo");
        }

        String sql = "{call modificar_pago(?, ?, ?, ?, ?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", pago.getId());
            cmd.setString("p_codigo_pago", pago.getCodigoPago());
            cmd.setDouble("p_monto", pago.getMonto());
            if (pago.getFechaPago() != null) {
                cmd.setTimestamp("p_fecha_pago", Timestamp.valueOf(pago.getFechaPago()));
            } else {
                cmd.setNull("p_fecha_pago", Types.TIMESTAMP);
            }
            cmd.setString("p_estado", pago.getEstado().name());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar el pago");
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call eliminar_pago(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo eliminar (rechazar) el pago: no existe o ya no esta PENDIENTE");
            }
        }
    }

    /**
     * Arma un Pago a partir de la fila actual del ResultSet. Reserva se
     * reconstruye con reservaDAO.findById (Pago exige una Reserva no nula
     * por constructor), y luego se sobreescriben fechaPago/estado leidos
     * de la fila, porque el constructor normal de Pago siempre deja el
     * pago recien creado en PENDIENTE con fechaPago nula.
     */
    private Pago mapear(ResultSet rs) throws SQLException {
        Reserva reserva = reservaDAO.findById(rs.getInt("reserva_id"));
        if (reserva == null) {
            throw new SQLException(
                    "Inconsistencia de datos: el pago " + rs.getInt("id")
                            + " referencia la reserva " + rs.getInt("reserva_id") + ", que no existe");
        }

        Pago pago = new Pago(
                rs.getInt("id"),
                rs.getString("codigo_pago"),
                rs.getDouble("monto"),
                reserva);

        Timestamp fechaPago = rs.getTimestamp("fecha_pago");
        pago.setFechaPago(fechaPago != null ? fechaPago.toLocalDateTime() : null);
        pago.setEstado(EstadoPago.valueOf(rs.getString("estado")));
        return pago;
    }
}
