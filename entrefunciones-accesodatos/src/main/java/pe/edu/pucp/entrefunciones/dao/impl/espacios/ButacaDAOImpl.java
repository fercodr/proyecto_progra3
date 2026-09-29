package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import pe.edu.pucp.entrefunciones.dao.espacios.ButacaDAO;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;

import java.sql.Types;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ButacaDAOImpl implements ButacaDAO {

    @Override
    public List<Butaca> findAll() throws SQLException {
        String sql = "{call listar_butacas()}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql);
                ResultSet rs = cmd.executeQuery()) {

            List<Butaca> butacas = new ArrayList<>();
            while (rs.next()) {
                butacas.add(mapear(rs));
            }
            return butacas;
        }
    }

    @Override
    public List<Butaca> findBySalaId(int salaId) throws SQLException {
        String sql = "{call listar_butacas_por_sala(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_sala_id", salaId);

            try (ResultSet rs = cmd.executeQuery()) {
                List<Butaca> butacas = new ArrayList<>();
                while (rs.next()) {
                    butacas.add(mapear(rs));
                }
                return butacas;
            }
        }
    }

    @Override
    public Butaca findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_butaca_por_id(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Butaca()) : null;
            }
        }
    }

    @Override
    public void insert(Butaca butaca) throws SQLException {
        if (butaca == null) {
            throw new IllegalArgumentException("La butaca no puede ser nula");
        }

        String sql = "{call insertar_butaca(?, ?, ?, ?, ?, ?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_sala_id", butaca.getSala().getId());
            cmd.setString("p_fila", butaca.getFila());
            cmd.setInt("p_columna", butaca.getColumna());
            cmd.setString("p_tipo_butaca", butaca.getTipoButaca());
            cmd.setBoolean("p_activo", butaca.estaActivo());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la butaca");
            }

            butaca.setId(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(Butaca butaca) throws SQLException {
        if (butaca == null) {
            throw new IllegalArgumentException("La butaca no puede ser nula");
        }

        String sql = "{call modificar_butaca(?, ?, ?, ?, ?, ?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", butaca.getId());
            cmd.setInt("p_sala_id", butaca.getSala().getId());
            cmd.setString("p_fila", butaca.getFila());
            cmd.setInt("p_columna", butaca.getColumna());
            cmd.setString("p_tipo_butaca", butaca.getTipoButaca());
            cmd.setBoolean("p_activo", butaca.estaActivo());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la butaca");
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call eliminar_butaca(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar la butaca");
            }
        }
    }
    @Override
    public List<Butaca> findByReservaId(int reservaId) throws SQLException {
        String sql = "{call listar_butacas_por_reserva(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt(1, reservaId); // O cmd.setInt("p_reserva_id", reservaId);

            try (ResultSet rs = cmd.executeQuery()) {
                List<Butaca> butacas = new ArrayList<>();
                while (rs.next()) {
                    butacas.add(mapear(rs));
                }
                return butacas;
            }
        }
    }

    private Butaca mapear(ResultSet rs) throws SQLException {
        int salaId = rs.getInt("sala_id");
        Sala sala = new SalaDAOImpl().findById(salaId);

        Butaca butaca = new Butaca(
                rs.getInt("id"),
                sala,
                rs.getString("fila"),
                rs.getInt("columna"),
                rs.getString("tipo_butaca")
        );

        if (rs.getBoolean("activo")) butaca.activar();
        else butaca.desactivar();

        return butaca;
    }
}