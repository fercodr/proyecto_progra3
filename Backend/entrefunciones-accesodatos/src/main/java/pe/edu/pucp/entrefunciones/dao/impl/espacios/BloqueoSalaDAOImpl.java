package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.BloqueoSala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;

public class BloqueoSalaDAOImpl {
    private final SalaDAOImpl salas = new SalaDAOImpl();

    public List<BloqueoSala> findAll() throws SQLException {
        String sql = "SELECT id, sala_id, inicio, fin, activo FROM bloqueo_sala ORDER BY inicio DESC, id DESC";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             PreparedStatement cmd = scope.connection().prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            List<BloqueoSala> resultado = new ArrayList<>();
            while (rs.next()) resultado.add(mapear(rs));
            return resultado;
        }
    }

    public BloqueoSala findById(int id) throws SQLException {
        String sql = "SELECT id, sala_id, inicio, fin, activo FROM bloqueo_sala WHERE id=?";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             PreparedStatement cmd = scope.connection().prepareStatement(sql)) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public void insert(BloqueoSala bloqueo) throws SQLException {
        String sql = "INSERT INTO bloqueo_sala(sala_id,inicio,fin,activo) VALUES(?,?,?,?)";
        try (PreparedStatement cmd = TransactionsManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            parametros(cmd, bloqueo, false);
            cmd.executeUpdate();
            try (ResultSet ids = cmd.getGeneratedKeys()) {
                if (!ids.next()) throw new SQLException("No se obtuvo el id del bloqueo");
                bloqueo.setId(ids.getInt(1));
            }
        }
    }

    public void update(BloqueoSala bloqueo) throws SQLException {
        String sql = "UPDATE bloqueo_sala SET sala_id=?,inicio=?,fin=?,activo=? WHERE id=?";
        try (PreparedStatement cmd = TransactionsManager.getConnection().prepareStatement(sql)) {
            parametros(cmd, bloqueo, true);
            if (cmd.executeUpdate() == 0) throw new SQLException("No existe el bloqueo indicado");
        }
    }

    public boolean existeSolapamiento(int salaId, LocalDateTime inicio,
                                      LocalDateTime fin, Integer excluirId)
            throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM bloqueo_sala WHERE sala_id=? AND activo=TRUE "
                + "AND inicio<? AND fin>? AND (? IS NULL OR id<>?))";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             PreparedStatement cmd = scope.connection().prepareStatement(sql)) {
            cmd.setInt(1, salaId);
            cmd.setTimestamp(2, Timestamp.valueOf(fin));
            cmd.setTimestamp(3, Timestamp.valueOf(inicio));
            if (excluirId == null) {
                cmd.setNull(4, java.sql.Types.INTEGER);
                cmd.setNull(5, java.sql.Types.INTEGER);
            } else {
                cmd.setInt(4, excluirId);
                cmd.setInt(5, excluirId);
            }
            try (ResultSet rs = cmd.executeQuery()) { rs.next(); return rs.getBoolean(1); }
        }
    }

    private BloqueoSala mapear(ResultSet rs) throws SQLException {
        Sala sala = salas.findById(rs.getInt("sala_id"));
        if (sala == null) throw new SQLException("El bloqueo referencia una sala inexistente");
        BloqueoSala bloqueo = new BloqueoSala(
                rs.getInt("id"), sala,
                rs.getTimestamp("inicio").toLocalDateTime(),
                rs.getTimestamp("fin").toLocalDateTime());
        if (!rs.getBoolean("activo")) bloqueo.desactivar();
        return bloqueo;
    }

    private void parametros(PreparedStatement cmd, BloqueoSala bloqueo, boolean incluirId)
            throws SQLException {
        cmd.setInt(1, bloqueo.getSala().getId());
        cmd.setTimestamp(2, Timestamp.valueOf(bloqueo.getInicio()));
        cmd.setTimestamp(3, Timestamp.valueOf(bloqueo.getFin()));
        cmd.setBoolean(4, bloqueo.estaActivo());
        if (incluirId) cmd.setInt(5, bloqueo.getId());
    }
}
