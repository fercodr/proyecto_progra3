package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import pe.edu.pucp.entrefunciones.dao.espacios.RecursoDAO;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Recurso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RecursoDAOImpl implements RecursoDAO {

    @Override
    public List<Recurso> findAll() throws SQLException {
        String sql = """
                SELECT id, nombre, tipo, activo
                FROM recurso
                """;
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            List<Recurso> recursos = new ArrayList<>();
            while (rs.next()) {
                recursos.add(mapear(rs));
            }
            return recursos;
        }
    }

    @Override
    public Recurso findById(Integer id) throws SQLException {
        validarId(id);
        String sql = """
                SELECT id, nombre, tipo, activo
                FROM recurso
                WHERE id = ?
                """;
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insert(Recurso recurso) throws SQLException {
        validarRecurso(recurso);
        String sql = """
                INSERT INTO recurso (nombre, tipo, activo)
                VALUES (?, ?, ?)
                """;
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, recurso.getNombre());
            cmd.setString(2, recurso.getTipo());
            cmd.setBoolean(3, recurso.estaActivo());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar el recurso");
            }
            asignarIdGenerado(cmd, recurso);
        }
    }

    @Override
    public void update(Recurso recurso) throws SQLException {
        validarRecurso(recurso);
        String sql = """
                UPDATE recurso
                SET nombre = ?, tipo = ?, activo = ?
                WHERE id = ?
                """;
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, recurso.getNombre());
            cmd.setString(2, recurso.getTipo());
            cmd.setBoolean(3, recurso.estaActivo());
            cmd.setInt(4, recurso.getId());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar el recurso con id " + recurso.getId());
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        validarId(id);
        String sql = """
                UPDATE recurso
                SET activo = FALSE
                WHERE id = ?
                """;
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, id);
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar el recurso con id " + id);
            }
        }
    }

    private Recurso mapear(ResultSet rs) throws SQLException {
        Recurso recurso = new Recurso();
        recurso.setId(rs.getInt("id"));
        recurso.setNombre(rs.getString("nombre"));
        recurso.setTipo(rs.getString("tipo"));
        if (rs.getBoolean("activo")) {
            recurso.activar();
        } else {
            recurso.desactivar();
        }
        return recurso;
    }

    private void asignarIdGenerado(PreparedStatement cmd, Recurso recurso) throws SQLException {
        try (ResultSet keys = cmd.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("No se obtuvo el id del recurso insertado");
            }
            recurso.setId(keys.getInt(1));
        }
    }

    private void validarRecurso(Recurso recurso) {
        if (recurso == null) {
            throw new IllegalArgumentException("El recurso no puede ser nulo");
        }
    }

    private void validarId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
    }
}
