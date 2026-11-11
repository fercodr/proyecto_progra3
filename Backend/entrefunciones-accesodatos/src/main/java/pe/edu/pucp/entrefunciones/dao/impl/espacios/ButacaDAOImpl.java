package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import pe.edu.pucp.entrefunciones.dao.espacios.ButacaDAO;
import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ButacaDAOImpl implements ButacaDAO {

    @Override
    public List<Butaca> findAll() throws SQLException {
        String sql = """
                SELECT b.id, b.fila, b.columna, b.tipo_butaca,
                       b.activo AS butaca_activo,
                       b.sala_id AS sala_id, sa.nombre AS sala_nombre,
                       sa.aforo AS sala_aforo, sa.tipo_sala,
                       sa.activo AS sala_activo,
                       se.id AS sede_id, se.razon_social AS sede_razon_social,
                       se.direccion AS sede_direccion, se.ciudad AS sede_ciudad,
                       se.telefono_contacto AS sede_telefono_contacto,
                       se.activo AS sede_activo
                FROM butaca b
                INNER JOIN sala sa ON sa.id = b.sala_id
                INNER JOIN sede se ON se.id = sa.sede_id
                """;
        try (ConnectionScope scope = ConnectionScope.openForRead();
             PreparedStatement cmd = scope.connection().prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            return mapearLista(rs);
        }
    }

    @Override
    public List<Butaca> findBySalaId(int salaId) throws SQLException {
        String sql = """
                SELECT b.id, b.fila, b.columna, b.tipo_butaca,
                       b.activo AS butaca_activo,
                       b.sala_id AS sala_id, sa.nombre AS sala_nombre,
                       sa.aforo AS sala_aforo, sa.tipo_sala,
                       sa.activo AS sala_activo,
                       se.id AS sede_id, se.razon_social AS sede_razon_social,
                       se.direccion AS sede_direccion, se.ciudad AS sede_ciudad,
                       se.telefono_contacto AS sede_telefono_contacto,
                       se.activo AS sede_activo
                FROM butaca b
                INNER JOIN sala sa ON sa.id = b.sala_id
                INNER JOIN sede se ON se.id = sa.sede_id
                WHERE b.sala_id = ?
                """;
        try (ConnectionScope scope = ConnectionScope.openForRead();
             PreparedStatement cmd = scope.connection().prepareStatement(sql)) {
            cmd.setInt(1, salaId);
            try (ResultSet rs = cmd.executeQuery()) {
                return mapearLista(rs);
            }
        }
    }

    @Override
    public List<Butaca> findByReservaId(int reservaId) throws SQLException {
        String sql = """
                SELECT b.id, b.fila, b.columna, b.tipo_butaca,
                       b.activo AS butaca_activo,
                       b.sala_id AS sala_id, sa.nombre AS sala_nombre,
                       sa.aforo AS sala_aforo, sa.tipo_sala,
                       sa.activo AS sala_activo,
                       se.id AS sede_id, se.razon_social AS sede_razon_social,
                       se.direccion AS sede_direccion, se.ciudad AS sede_ciudad,
                       se.telefono_contacto AS sede_telefono_contacto,
                       se.activo AS sede_activo
                FROM butaca b
                INNER JOIN sala sa ON sa.id = b.sala_id
                INNER JOIN sede se ON se.id = sa.sede_id
                INNER JOIN reserva_butaca rb ON rb.butaca_id = b.id
                WHERE rb.reserva_id = ?
                """;
        try (ConnectionScope scope = ConnectionScope.openForRead();
             PreparedStatement cmd = scope.connection().prepareStatement(sql)) {
            cmd.setInt(1, reservaId);
            try (ResultSet rs = cmd.executeQuery()) {
                return mapearLista(rs);
            }
        }
    }

    @Override
    public Butaca findById(Integer id) throws SQLException {
        validarId(id);
        String sql = """
                SELECT b.id, b.fila, b.columna, b.tipo_butaca,
                       b.activo AS butaca_activo,
                       b.sala_id AS sala_id, sa.nombre AS sala_nombre,
                       sa.aforo AS sala_aforo, sa.tipo_sala,
                       sa.activo AS sala_activo,
                       se.id AS sede_id, se.razon_social AS sede_razon_social,
                       se.direccion AS sede_direccion, se.ciudad AS sede_ciudad,
                       se.telefono_contacto AS sede_telefono_contacto,
                       se.activo AS sede_activo
                FROM butaca b
                INNER JOIN sala sa ON sa.id = b.sala_id
                INNER JOIN sede se ON se.id = sa.sede_id
                WHERE b.id = ?
                """;
        try (ConnectionScope scope = ConnectionScope.openForRead();
             PreparedStatement cmd = scope.connection().prepareStatement(sql)) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insert(Butaca butaca) throws SQLException {
        validarButaca(butaca);
        String sql = """
                INSERT INTO butaca (sala_id, fila, columna, tipo_butaca, activo)
                VALUES (?, ?, ?, ?, ?)
                """;
        Connection conn = TransactionsManager.getConnection();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(cmd, butaca);
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la butaca");
            }
            asignarIdGenerado(cmd, butaca);
        }
    }

    /**
     * Inserta todas las butacas de una sala en un solo batch, dentro de la transacción activa.
     * Pensado para ser llamado desde SalaDAOImpl.insert una vez que la sala ya tiene su id.
     */
    @Override
    public void insertButacas(int salaId, List<Butaca> butacas) throws SQLException {
        validarId(salaId);
        if (butacas == null || butacas.isEmpty()) {
            return;
        }
        String sql = """
                INSERT INTO butaca (sala_id, fila, columna, tipo_butaca, activo)
                VALUES (?, ?, ?, ?, ?)
                """;
        Connection conn = TransactionsManager.getConnection();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Butaca butaca : butacas) {
                if (butaca == null) {
                    throw new IllegalArgumentException("La butaca no puede ser nula");
                }
                cmd.setInt(1, salaId);
                cmd.setString(2, butaca.getFila());
                cmd.setInt(3, butaca.getColumna());
                cmd.setString(4, butaca.getTipoButaca());
                cmd.setBoolean(5, butaca.estaActivo());
                cmd.addBatch();
            }
            cmd.executeBatch();

            // Los ids se devuelven en el mismo orden en que se agregaron al batch
            try (ResultSet keys = cmd.getGeneratedKeys()) {
                int i = 0;
                while (i < butacas.size() && keys.next()) {
                    butacas.get(i++).setId(keys.getInt(1));
                }
            }
        }
    }

    /** Elimina todas las butacas de una sala (dentro de la transacción activa). */
    @Override
    public void deleteButacas(int salaId) throws SQLException {
        validarId(salaId);
        String sql = "DELETE FROM butaca WHERE sala_id = ?";
        Connection conn = TransactionsManager.getConnection();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, salaId);
            cmd.executeUpdate();
        }
    }

    @Override
    public void update(Butaca butaca) throws SQLException {
        validarButaca(butaca);
        String sql = """
                UPDATE butaca
                SET sala_id = ?, fila = ?, columna = ?, tipo_butaca = ?, activo = ?
                WHERE id = ?
                """;
        Connection conn = TransactionsManager.getConnection();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            asignarParametros(cmd, butaca);
            cmd.setInt(6, butaca.getId());
            if (cmd.executeUpdate() == 0 && !existe(conn, butaca.getId())) {
                throw new SQLException("No se pudo actualizar la butaca con id " + butaca.getId());
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        validarId(id);
        String sql = """
                UPDATE butaca
                SET activo = FALSE
                WHERE id = ?
                """;
        Connection conn = TransactionsManager.getConnection();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, id);
            if (cmd.executeUpdate() == 0 && !existe(conn, id)) {
                throw new SQLException("No se pudo eliminar la butaca con id " + id);
            }
        }
    }

    private boolean existe(Connection conn, int id) throws SQLException {
        try (PreparedStatement cmd = conn.prepareStatement(
                "SELECT 1 FROM butaca WHERE id = ?")) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void asignarParametros(PreparedStatement cmd, Butaca butaca) throws SQLException {
        cmd.setInt(1, butaca.getSala().getId());
        cmd.setString(2, butaca.getFila());
        cmd.setInt(3, butaca.getColumna());
        cmd.setString(4, butaca.getTipoButaca());
        cmd.setBoolean(5, butaca.estaActivo());
    }

    private List<Butaca> mapearLista(ResultSet rs) throws SQLException {
        List<Butaca> butacas = new ArrayList<>();
        while (rs.next()) {
            butacas.add(mapear(rs));
        }
        return butacas;
    }

    private Butaca mapear(ResultSet rs) throws SQLException {
        Sede sede = new Sede();
        sede.setId(rs.getInt("sede_id"));
        sede.setRazonSocial(rs.getString("sede_razon_social"));
        sede.setDireccion(rs.getString("sede_direccion"));
        sede.setCiudad(rs.getString("sede_ciudad"));
        sede.setTelefonoContacto(rs.getString("sede_telefono_contacto"));
        if (rs.getBoolean("sede_activo")) {
            sede.activar();
        } else {
            sede.desactivar();
        }

        Sala sala = new Sala(
                rs.getInt("sala_id"),
                sede,
                rs.getString("sala_nombre"),
                rs.getInt("sala_aforo"),
                rs.getString("tipo_sala")
        );
        if (rs.getBoolean("sala_activo")) {
            sala.activar();
        } else {
            sala.desactivar();
        }

        Butaca butaca = new Butaca(
                rs.getInt("id"),
                sala,
                rs.getString("fila"),
                rs.getInt("columna"),
                rs.getString("tipo_butaca")
        );
        if (rs.getBoolean("butaca_activo")) {
            butaca.activar();
        } else {
            butaca.desactivar();
        }
        return butaca;
    }

    private void asignarIdGenerado(PreparedStatement cmd, Butaca butaca) throws SQLException {
        try (ResultSet keys = cmd.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("No se obtuvo el id de la butaca insertada");
            }
            butaca.setId(keys.getInt(1));
        }
    }

    private void validarButaca(Butaca butaca) {
        if (butaca == null) {
            throw new IllegalArgumentException("La butaca no puede ser nula");
        }
        if (butaca.getSala() == null) {
            throw new IllegalArgumentException("La sala de la butaca no puede ser nula");
        }
    }

    private void validarId(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id debe ser positivo");
        }
    }
}
