package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import pe.edu.pucp.entrefunciones.dao.espacios.SalaDAO;
import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SalaDAOImpl implements SalaDAO {

    @Override
    public List<Sala> findAll() throws SQLException {
        String sql = "{call listar_salas()}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            List<Sala> salas = new ArrayList<>();
            while (rs.next()) {
                salas.add(mapear(rs));
            }
            return salas;
        }
    }

    @Override
    public List<Sala> findBySedeId(int sedeId) throws SQLException {
        validarId(sedeId);
        String sql = "{call listar_salas_por_sede(?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {
            cmd.setInt(1, sedeId);
            try (ResultSet rs = cmd.executeQuery()) {
                List<Sala> salas = new ArrayList<>();
                while (rs.next()) {
                    salas.add(mapear(rs));
                }
                return salas;
            }
        }
    }

    @Override
    public Sala findById(Integer id) throws SQLException {
        validarId(id);
        String sql = "{call buscar_sala_por_id(?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insert(Sala sala) throws SQLException {
        validarSala(sala);
        String sql = "{call insertar_sala(?, ?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.getConnection();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            asignarParametros(cmd, sala);
            cmd.registerOutParameter(6, Types.INTEGER);
            cmd.executeUpdate();
            sala.setId(cmd.getInt(6));
            if (sala.getId() <= 0) {
                throw new SQLException("No se obtuvo el id de la sala insertada");
            }
        }
    }

    @Override
    public void update(Sala sala) throws SQLException {
        validarSala(sala);
        validarId(sala.getId());
        String sql = "{call modificar_sala(?, ?, ?, ?, ?, ?)}";
        Connection conn = TransactionsManager.getConnection();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt(1, sala.getId());
            cmd.setInt(2, sala.getSede().getId());
            cmd.setString(3, sala.getNombre());
            cmd.setInt(4, sala.getAforo());
            cmd.setString(5, sala.getTipoSala());
            cmd.setBoolean(6, sala.estaActivo());
            cmd.executeUpdate();
            if (!existe(conn, sala.getId())) {
                throw new SQLException("No se pudo actualizar la sala con id " + sala.getId());
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        validarId(id);
        String sql = "{call eliminar_sala(?)}";
        Connection conn = TransactionsManager.getConnection();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt(1, id);
            cmd.executeUpdate();
            if (!existe(conn, id)) {
                throw new SQLException("No se pudo eliminar la sala con id " + id);
            }
        }
    }

    private boolean existe(Connection conn, int id) throws SQLException {
        try (PreparedStatement cmd = conn.prepareStatement(
                "SELECT 1 FROM sala WHERE id = ?")) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void asignarParametros(CallableStatement cmd, Sala sala) throws SQLException {
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

    private void validarSala(Sala sala) {
        if (sala == null) {
            throw new IllegalArgumentException("La sala no puede ser nula");
        }
        if (sala.getSede() == null) {
            throw new IllegalArgumentException("La sede de la sala no puede ser nula");
        }
    }

    private void validarId(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id debe ser positivo");
        }
    }
}
