package pe.edu.pucp.entrefunciones.dao.impl.catalogo;

import pe.edu.pucp.entrefunciones.dao.catalogo.ActividadCulturalDAO;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ActividadCultural;
import pe.edu.pucp.entrefunciones.modelo.catalogo.TipoActividad;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActividadCulturalDAOImpl implements ActividadCulturalDAO {

    @Override
    public List<ActividadCultural> findAll() throws SQLException {
        String sql = "{call listar_actividades_culturales()}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql);
                ResultSet rs = cmd.executeQuery()) {

            List<ActividadCultural> actividades = new ArrayList<>();
            while (rs.next()) {
                actividades.add(mapear(rs));
            }
            return actividades;
        }
    }

    @Override
    public ActividadCultural findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_actividad_cultural_por_id(?)}";
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
    public void insert(ActividadCultural actividad) throws SQLException {
        if (actividad == null) {
            throw new IllegalArgumentException(
                    "La actividad cultural no puede ser nula");
        }

        String sql = "{call insertar_actividad_cultural(?, ?, ?, ?, ?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setDouble("p_precio", actividad.getPrecio());
            cmd.setString("p_nombre", actividad.getNombre());
            cmd.setString("p_expositor", actividad.getExpositor());
            cmd.setString("p_tipo_actividad",
                    actividad.getTipoActividad().name());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo insertar la actividad cultural");
            }

            actividad.setId(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(ActividadCultural actividad) throws SQLException {
        if (actividad == null) {
            throw new IllegalArgumentException(
                    "La actividad cultural no puede ser nula");
        }

        String sql = "{call modificar_actividad_cultural(?, ?, ?, ?, ?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setDouble("p_precio", actividad.getPrecio());
            cmd.setString("p_nombre", actividad.getNombre());
            cmd.setString("p_expositor", actividad.getExpositor());
            cmd.setString("p_tipo_actividad",
                    actividad.getTipoActividad().name());
            cmd.setInt("p_id", actividad.getId());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo actualizar la actividad cultural");
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call eliminar_actividad_cultural(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo eliminar la actividad cultural");
            }
        }
    }


    private ActividadCultural mapear(ResultSet rs) throws SQLException {
        return new ActividadCultural(
                rs.getInt("id_evento"),
                rs.getDouble("precio"),
                rs.getString("nombre"),
                rs.getString("expositor"),
                TipoActividad.valueOf(rs.getString("tipo_actividad"))
        );
    }
}