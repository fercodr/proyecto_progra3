package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import pe.edu.pucp.entrefunciones.dao.espacios.SedeDAO;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SedeDAOImpl implements SedeDAO {

    @Override
    public List<Sede> findAll() throws SQLException {
        String sql = """
                SELECT id, razon_social, direccion, ciudad, telefono_contacto, activo
                FROM sede
                """;
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            List<Sede> sedes = new ArrayList<>();
            while (rs.next()) {
                sedes.add(mapear(rs));
            }
            return sedes;
        }
    }

    @Override
    public Sede findById(Integer id) throws SQLException {
        validarId(id);
        String sql = """
                SELECT id, razon_social, direccion, ciudad, telefono_contacto, activo
                FROM sede
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
    public void insert(Sede sede) throws SQLException {
        validarSede(sede);
        String sql = """
                INSERT INTO sede (razon_social, direccion, ciudad, telefono_contacto, activo)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(cmd, sede);
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la sede");
            }
            asignarIdGenerado(cmd, sede);
        }
    }

    @Override
    public void update(Sede sede) throws SQLException {
        validarSede(sede);
        String sql = """
                UPDATE sede
                SET razon_social = ?, direccion = ?, ciudad = ?, telefono_contacto = ?, activo = ?
                WHERE id = ?
                """;
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql)) {
            asignarParametros(cmd, sede);
            cmd.setInt(6, sede.getId());
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la sede con id " + sede.getId());
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        validarId(id);
        String sql = """
                UPDATE sede
                SET activo = FALSE
                WHERE id = ?
                """;
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, id);
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar la sede con id " + id);
            }
        }
    }

    private void asignarParametros(PreparedStatement cmd, Sede sede) throws SQLException {
        cmd.setString(1, sede.getRazonSocial());
        cmd.setString(2, sede.getDireccion());
        cmd.setString(3, sede.getCiudad());
        cmd.setString(4, sede.getTelefonoContacto());
        cmd.setBoolean(5, sede.estaActivo());
    }

    private Sede mapear(ResultSet rs) throws SQLException {
        Sede sede = new Sede();
        sede.setId(rs.getInt("id"));
        sede.setRazonSocial(rs.getString("razon_social"));
        sede.setDireccion(rs.getString("direccion"));
        sede.setCiudad(rs.getString("ciudad"));
        sede.setTelefonoContacto(rs.getString("telefono_contacto"));
        if (rs.getBoolean("activo")) {
            sede.activar();
        } else {
            sede.desactivar();
        }
        return sede;
    }

    private void asignarIdGenerado(PreparedStatement cmd, Sede sede) throws SQLException {
        try (ResultSet keys = cmd.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("No se obtuvo el id de la sede insertada");
            }
            sede.setId(keys.getInt(1));
        }
    }

    private void validarSede(Sede sede) {
        if (sede == null) {
            throw new IllegalArgumentException("La sede no puede ser nula");
    }
    }

    private void validarId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
    }
}
