package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import pe.edu.pucp.entrefunciones.dao.espacios.BloqueoDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Bloqueo;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BloqueoDAOImpl implements BloqueoDAO {

    @Override
    public List<Bloqueo> findAll() throws SQLException { //devuelve una lista de todos los bloqueos posibles
        String sql = "{call listar_bloqueos()}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql);
                ResultSet rs = cmd.executeQuery()) {

            List<Bloqueo> bloqueos = new ArrayList<>();
            while (rs.next()) {
                bloqueos.add(mapear(rs, new Bloqueo()));
            }
            return bloqueos;
        }
    }
    //devuelve una lista de todos los bloqueos por id de sala
    @Override
    public List<Bloqueo> findBySalaId(int salaId) throws SQLException {
        String sql = "{call listar_bloqueos_por_sala(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_sala_id", salaId);

            try (ResultSet rs = cmd.executeQuery()) {
                List<Bloqueo> bloqueos = new ArrayList<>();
                while (rs.next()) {
                    bloqueos.add(mapear(rs, new Bloqueo()));
                }
                return bloqueos;
            }
        }
    }
    //enlista todos los bloqueos realizados a todos los recursos
    @Override
    public List<Bloqueo> findByRecursoId(int recursoId) throws SQLException {
        String sql = "{call listar_bloqueos_por_recurso(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_recurso_id", recursoId);

            try (ResultSet rs = cmd.executeQuery()) {
                List<Bloqueo> bloqueos = new ArrayList<>();
                while (rs.next()) {
                    bloqueos.add(mapear(rs, new Bloqueo()));
                }
                return bloqueos;
            }
        }
    }

    @Override
    public Bloqueo findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_bloqueo_por_id(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Bloqueo()) : null;
            }
        }
    }

    @Override
    public void insert(Bloqueo bloqueo) throws SQLException {
        if (bloqueo == null) {
            throw new IllegalArgumentException("El bloqueo no puede ser nulo");
        }

        String sql = "{call insertar_bloqueo(?, ?, ?, ?, ?, ?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setString("p_motivo", bloqueo.getMotivo());
            cmd.setTimestamp("p_fecha_inicio", Timestamp.valueOf(bloqueo.getFechaInicio()));
            cmd.setTimestamp("p_fecha_fin", Timestamp.valueOf(bloqueo.getFechaFin()));

            if (bloqueo.getSala() != null) {
                cmd.setInt("p_sala_id", bloqueo.getSala().getId());
                cmd.setNull("p_recurso_id", Types.INTEGER);
            } else {
                cmd.setNull("p_sala_id", Types.INTEGER);
                cmd.setInt("p_recurso_id", bloqueo.getRecurso().getId());
            }
            //es la variable que trae consigo el insertar_bloqueo, por eso se coloca registerOutParameter
            //como no existe, tengo que completarlo con el dato que me dé la BD
            cmd.registerOutParameter("p_id", Types.INTEGER); //types.INTEGER Porque necesitas especificar que tipo de dato es antes de setear un set(null)
            //el programa debe saber en qué tipo de dato esta que coloca el null
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar el bloqueo");
            }

            bloqueo.setId(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(Bloqueo bloqueo) throws SQLException {
        if (bloqueo == null) {
            throw new IllegalArgumentException("El bloqueo no puede ser nulo");
        }

        String sql = "{call modificar_bloqueo(?, ?, ?, ?, ?, ?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", bloqueo.getId());
            cmd.setString("p_motivo", bloqueo.getMotivo());
            cmd.setTimestamp("p_fecha_inicio", Timestamp.valueOf(bloqueo.getFechaInicio()));
            cmd.setTimestamp("p_fecha_fin", Timestamp.valueOf(bloqueo.getFechaFin()));

            if (bloqueo.getSala() != null) {
                cmd.setInt("p_sala_id", bloqueo.getSala().getId());
                cmd.setNull("p_recurso_id", Types.INTEGER);
            } else {
                cmd.setNull("p_sala_id", Types.INTEGER);
                cmd.setInt("p_recurso_id", bloqueo.getRecurso().getId());
            }
            //aqui no es necesario registerOutParameter porque ya existe el dato, solo estoy actualizando
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar el bloqueo");
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call eliminar_bloqueo(?)}";
        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)) {

            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar el bloqueo");
            }
        }
    }

    private Bloqueo mapear(ResultSet rs, Bloqueo bloqueo) throws SQLException {
        bloqueo.setId(rs.getInt("id"));
        bloqueo.setMotivo(rs.getString("motivo"));
        bloqueo.setFechaInicio(rs.getTimestamp("fecha_inicio").toLocalDateTime());
        bloqueo.setFechaFin(rs.getTimestamp("fecha_fin").toLocalDateTime());

        int salaId = rs.getInt("sala_id");
        if (!rs.wasNull()) {
            bloqueo.setSala(new SalaDAOImpl().findById(salaId));
        } else {
            bloqueo.setSala(null);
        }

        int recursoId = rs.getInt("recurso_id");
        if (!rs.wasNull()) {
            bloqueo.setRecurso(new RecursoDAOImpl().findById(recursoId));
        } else {
            bloqueo.setRecurso(null);
        }

        return bloqueo;
    }
}