package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import pe.edu.pucp.entrefunciones.dao.espacios.SalaDAO;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SalaDAOImpl implements SalaDAO {

    @Override
    public List<Sala> findAll() throws SQLException {
        String sql = """
                SELECT sa.id, sa.nombre, sa.aforo, sa.tipo_sala, sa.activo,
                       sa.sede_id AS sede_id, se.razon_social AS sede_razon_social,
                       se.direccion AS sede_direccion, se.ciudad AS sede_ciudad,
                       se.telefono_contacto AS sede_telefono_contacto,
                       se.activo AS sede_activo
                FROM sala sa
                INNER JOIN sede se ON se.id = sa.sede_id
                """;
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            List<Sala> salas = new ArrayList<>();
            while (rs.next()) {
                salas.add(mapear(rs));
            }
            return salas;
        }
    }

    @Override
    public Sala findById(Integer id) throws SQLException {
        validarId(id);
        String sql = """
                SELECT sa.id, sa.nombre, sa.aforo, sa.tipo_sala, sa.activo,
                       sa.sede_id AS sede_id, se.razon_social AS sede_razon_social,
                       se.direccion AS sede_direccion, se.ciudad AS sede_ciudad,
                       se.telefono_contacto AS sede_telefono_contacto,
                       se.activo AS sede_activo
                FROM sala sa
                INNER JOIN sede se ON se.id = sa.sede_id
                WHERE sa.id = ?
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
    public void insert(Sala sala) throws SQLException {
        validarSala(sala);
        String sql = """
                INSERT INTO sala (sede_id, nombre, aforo, tipo_sala, activo)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(cmd, sala);
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la sala");
            }
            asignarIdGenerado(cmd, sala);
        }
    }

    @Override
    public void update(Sala sala) throws SQLException {
        validarSala(sala);
        String sql = """
                UPDATE sala
                SET sede_id = ?, nombre = ?, aforo = ?, tipo_sala = ?, activo = ?
                WHERE id = ?
                """;
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql)) {
            asignarParametros(cmd, sala);
            cmd.setInt(6, sala.getId());
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la sala con id " + sala.getId());
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        validarId(id);
        String sql = """
                UPDATE sala
                SET activo = FALSE
                WHERE id = ?
                """;
        try (Connection conn = DBManager.getInstance().getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, id);
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar la sala con id " + id);
            }
        }
    }

    private void asignarParametros(PreparedStatement cmd, Sala sala) throws SQLException {
        cmd.setInt(1, sala.getSede().getId());
        cmd.setString(2, sala.getNombre());
        cmd.setInt(3, sala.getAforo());
        cmd.setString(4, sala.getTipoSala());
        cmd.setBoolean(5, sala.estaActivo());
    }

    private Sala mapear(ResultSet rs) throws SQLException {
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

        Sala sala = new Sala();
        sala.setId(rs.getInt("id"));
        sala.setSede(sede);
        sala.setNombre(rs.getString("nombre"));
        sala.setAforo(rs.getInt("aforo"));
        sala.setTipoSala(rs.getString("tipo_sala"));
        if (rs.getBoolean("activo")) {
            sala.activar();
        } else {
            sala.desactivar();
        }
        return sala;
    }

    private void asignarIdGenerado(PreparedStatement cmd, Sala sala) throws SQLException {
        try (ResultSet keys = cmd.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("No se obtuvo el id de la sala insertada");
            }
            sala.setId(keys.getInt(1));
        }
    }

    private void validarSala(Sala sala) {
        if (sala == null) {
            throw new IllegalArgumentException("La sala no puede ser nula");
        }
        if (sala.getSede() == null) {
            throw new IllegalArgumentException("La sede de la sala no puede ser nula");
        }
    }

    private void validarId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
    }
}
