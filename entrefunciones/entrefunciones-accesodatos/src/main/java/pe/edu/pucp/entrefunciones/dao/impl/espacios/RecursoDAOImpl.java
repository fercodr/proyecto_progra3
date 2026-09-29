package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import pe.edu.pucp.entrefunciones.dao.espacios.RecursoDAO;
import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Recurso;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class RecursoDAOImpl implements RecursoDAO {

    @Override
    public List<Recurso> findAll() throws SQLException {
        String sql = "{call listar_recursos()}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql);
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
        String sql = "{call buscar_recurso_por_id(?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insert(Recurso recurso) throws SQLException {
        validarRecurso(recurso);
        String sql = "{call insertar_recurso(?, ?, ?, ?)}";
        Connection conn = TransactionsManager.getConnection();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString(1, recurso.getNombre());
            cmd.setString(2, recurso.getTipo());
            cmd.setBoolean(3, recurso.estaActivo());
            cmd.registerOutParameter(4, Types.INTEGER);
            cmd.executeUpdate();
            recurso.setId(cmd.getInt(4));
            if (recurso.getId() <= 0) {
                throw new SQLException("No se obtuvo el id del recurso insertado");
            }
        }
    }

    @Override
    public void update(Recurso recurso) throws SQLException {
        validarRecurso(recurso);
        validarId(recurso.getId());
        String sql = "{call modificar_recurso(?, ?, ?, ?)}";
        Connection conn = TransactionsManager.getConnection();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt(1, recurso.getId());
            cmd.setString(2, recurso.getNombre());
            cmd.setString(3, recurso.getTipo());
            cmd.setBoolean(4, recurso.estaActivo());
            cmd.executeUpdate();
            if (!existe(conn, recurso.getId())) {
                throw new SQLException("No se pudo actualizar el recurso con id " + recurso.getId());
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        validarId(id);
        String sql = "{call eliminar_recurso(?)}";
        Connection conn = TransactionsManager.getConnection();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt(1, id);
            cmd.executeUpdate();
            if (!existe(conn, id)) {
                throw new SQLException("No se pudo eliminar el recurso con id " + id);
            }
        }
    }

    private boolean existe(Connection conn, int id) throws SQLException {
        try (PreparedStatement cmd = conn.prepareStatement(
                "SELECT 1 FROM recurso WHERE id = ?")) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next();
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

    private void validarRecurso(Recurso recurso) {
        if (recurso == null) {
            throw new IllegalArgumentException("El recurso no puede ser nulo");
        }
    }

    private void validarId(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id debe ser positivo");
        }
    }
}
