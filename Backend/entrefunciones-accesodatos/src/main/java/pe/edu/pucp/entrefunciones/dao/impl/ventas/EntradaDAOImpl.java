package pe.edu.pucp.entrefunciones.dao.impl.ventas;
import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pe.edu.pucp.entrefunciones.dao.espacios.ButacaDAO;
import pe.edu.pucp.entrefunciones.dao.ventas.EntradaDAO;
import pe.edu.pucp.entrefunciones.dao.ventas.ReservaDAO;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;
import pe.edu.pucp.entrefunciones.modelo.ventas.EstadoEntrada;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;


public class EntradaDAOImpl implements EntradaDAO {

    private final ReservaDAO reservaDAO;
    private final ButacaDAO butacaDAO;

    public EntradaDAOImpl(ReservaDAO reservaDAO, ButacaDAO butacaDAO) {
        if (reservaDAO == null) {
            throw new IllegalArgumentException("El reservaDAO no puede ser nulo");
        }
        if (butacaDAO == null) {
            throw new IllegalArgumentException("El butacaDAO no puede ser nulo");
        }
        this.reservaDAO = reservaDAO;
        this.butacaDAO = butacaDAO;
    }

    @Override
    public List<Entrada> findAll() throws SQLException {
        String sql = "{call listar_entradas()}";
        try (
                ConnectionScope scope = ConnectionScope.openForRead();
                CallableStatement cmd = scope.connection().prepareCall(sql);
                ResultSet rs = cmd.executeQuery()) {

            List<Entrada> entradas = new ArrayList<>();
            while (rs.next()) {
                entradas.add(mapear(rs));
            }
            return entradas;
        }
    }

    @Override
    public Entrada findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_entrada_por_id(?)}";
        try (
                ConnectionScope scope = ConnectionScope.openForRead();
                CallableStatement cmd = scope.connection().prepareCall(sql)) {

            cmd.setInt("p_id", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Entrada> findByReserva(int reservaId) throws SQLException {
        Reserva reserva = reservaDAO.findById(reservaId);
        if (reserva == null) {
            return new ArrayList<>();
        }

        Map<Integer, Butaca> butacasPorId = new HashMap<>();
        for (Butaca butaca : reserva.getButacas()) {
            butacasPorId.put(butaca.getId(), butaca);
        }

        String sql = "{call buscar_entrada_por_reserva(?)}";
        try (
                ConnectionScope scope = ConnectionScope.openForRead();
                CallableStatement cmd = scope.connection().prepareCall(sql)) {

            cmd.setInt("p_reserva_id", reservaId);

            try (ResultSet rs = cmd.executeQuery()) {
                List<Entrada> entradas = new ArrayList<>();
                while (rs.next()) {
                    entradas.add(mapearDeReserva(rs, reserva, butacasPorId));
                }
                return entradas;
            }
        }
    }

    @Override
    public Entrada findByCodigoQR(String codigoQR) throws SQLException {
        if (codigoQR == null || codigoQR.isEmpty()) {
            throw new IllegalArgumentException("El codigoQR no puede ser nulo o vacio");
        }

        String sql = "{call buscar_entrada_por_qr(?)}";
        try (
                ConnectionScope scope = ConnectionScope.openForRead();
                CallableStatement cmd = scope.connection().prepareCall(sql)) {

            cmd.setString("p_codigo_qr", codigoQR);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insert(Entrada entrada) throws SQLException {
        if (entrada == null) {
            throw new IllegalArgumentException("La entrada no puede ser nula");
        }

        String sql = "{call insertar_entrada(?, ?, ?, ?, ?, ?, ?)}";
        try (
                ConnectionScope scope = ConnectionScope.openForRead();
                CallableStatement cmd = scope.connection().prepareCall(sql)) {

            cmd.setInt("p_reserva_id", entrada.getReserva().getId());
            if (entrada.getButaca() == null) cmd.setNull("p_butaca_id", Types.INTEGER);
            else cmd.setInt("p_butaca_id", entrada.getButaca().getId());
            cmd.setTimestamp("p_fecha_emision", Timestamp.valueOf(entrada.getFechaEmision()));
            cmd.setString("p_codigo_qr", entrada.getCodigoQR());
            cmd.setDouble("p_precio_final", entrada.getPrecioFinal());
            cmd.setString("p_estado", entrada.getEstado().name());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la entrada");
            }

            entrada.setId(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(Entrada entrada) throws SQLException {
        if (entrada == null) {
            throw new IllegalArgumentException("La entrada no puede ser nula");
        }

        String sql = "{call modificar_entrada(?, ?, ?)}";
        try (
                ConnectionScope scope = ConnectionScope.openForRead();
                CallableStatement cmd = scope.connection().prepareCall(sql)) {

            cmd.setInt("p_id", entrada.getId());
            cmd.setString("p_estado", entrada.getEstado().name());
            cmd.setDouble("p_precio_final", entrada.getPrecioFinal());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la entrada");
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call eliminar_entrada(?)}";
        try (
                ConnectionScope scope = ConnectionScope.openForRead();
                CallableStatement cmd = scope.connection().prepareCall(sql)) {

            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo eliminar (cancelar) la entrada: no existe o ya no estaba VIGENTE");
            }
        }
    }

    /**
     * Arma una Entrada a partir de la fila actual del ResultSet, y luego
     * aplica el estado leido de la fila (el constructor siempre deja la
     * entrada recien creada en VIGENTE).
     */
    private Entrada mapear(ResultSet rs) throws SQLException {
        Reserva reserva = reservaDAO.findById(rs.getInt("reserva_id"));
        if (reserva == null) {
            throw new SQLException(
                    "Inconsistencia de datos: la entrada " + rs.getInt("id")
                            + " referencia la reserva " + rs.getInt("reserva_id") + ", que no existe");
        }

        int butacaId = rs.getInt("butaca_id");
        boolean general = rs.wasNull();
        Butaca butaca = general ? null : butacaDAO.findById(butacaId);
        if (!general && butaca == null) {
            throw new SQLException(
                    "Inconsistencia de datos: la entrada " + rs.getInt("id")
                            + " referencia la butaca " + butacaId + ", que no existe");
        }

        Entrada entrada = new Entrada(
                rs.getInt("id"),
                rs.getTimestamp("fecha_emision").toLocalDateTime(),
                rs.getString("codigo_qr"),
                rs.getDouble("precio_final"),
                butaca,
                reserva);

        entrada.setEstado(EstadoEntrada.valueOf(rs.getString("estado")));
        return entrada;
    }

    /**
     * Variante para listar las entradas de una misma reserva. Reutiliza el
     * agregado ya cargado y evita repetir todas sus consultas por cada fila.
     */
    private Entrada mapearDeReserva(ResultSet rs, Reserva reserva,
                                    Map<Integer, Butaca> butacasPorId)
            throws SQLException {
        int butacaId = rs.getInt("butaca_id");
        boolean general = rs.wasNull();
        Butaca butaca = general ? null : butacasPorId.get(butacaId);
        if (!general && butaca == null) {
            throw new SQLException(
                    "Inconsistencia de datos: la entrada " + rs.getInt("id")
                            + " referencia la butaca " + butacaId
                            + ", que no pertenece a la reserva "
                            + reserva.getId());
        }

        Entrada entrada = new Entrada(
                rs.getInt("id"),
                rs.getTimestamp("fecha_emision").toLocalDateTime(),
                rs.getString("codigo_qr"),
                rs.getDouble("precio_final"),
                butaca,
                reserva);
        entrada.setEstado(EstadoEntrada.valueOf(rs.getString("estado")));
        return entrada;
    }
}
