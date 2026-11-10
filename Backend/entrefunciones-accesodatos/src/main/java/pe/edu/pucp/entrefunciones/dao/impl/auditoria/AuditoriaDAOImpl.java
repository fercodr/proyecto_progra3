package pe.edu.pucp.entrefunciones.dao.impl.auditoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import pe.edu.pucp.entrefunciones.dao.auditoria.AuditoriaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.usuarios.AdministradorDAOImpl;
import pe.edu.pucp.entrefunciones.dao.usuarios.AdministradorDAO;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.auditoria.Auditoria;
import pe.edu.pucp.entrefunciones.modelo.auditoria.EstadoAuditoria;


public class AuditoriaDAOImpl implements AuditoriaDAO {

    @Override
    public List<Auditoria> findAll() throws SQLException {
        String sql = "{call listar_auditorias()}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            return mapearAuditorias(rs);
            }
        }
    @Override
    public Auditoria findById(Integer id) throws SQLException {
        if (id == null || id <= 0) throw new IllegalArgumentException("El id debe ser positivo");
        String sql = "{call buscar_auditoria_por_id(?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapearAuditoria(rs,new Auditoria()) : null;
            }
        }
    }
    @Override
    public List<Auditoria> findByFilters(String descripcion,EstadoAuditoria estado)
            throws SQLException {
        String sql = "{call listar_auditorias_filtradas(?, ?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            if (descripcion == null || descripcion.isBlank()) {
                cmd.setNull(1, Types.VARCHAR);
            } else {
                cmd.setString(1, descripcion.trim());
            }
            if (estado == null) {
                cmd.setNull(2, Types.VARCHAR);
            } else {
                cmd.setString(2, estado.name());
            }
            try (ResultSet rs = cmd.executeQuery()) {
                return mapearAuditorias(rs);
            }
        }
    }
    @Override
    public void insert(Auditoria auditoria) throws SQLException {
        validarAuditoriaEscribible(auditoria);
        String sql = "{call insertar_auditoria(?, ?, ?, ?, ?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt(1, auditoria.getFuncion().getId());
            cmd.setInt(2, auditoria.getResponsable().getId());
            cmd.setString(3, auditoria.getDescripcion());
            cmd.registerOutParameter(4, Types.INTEGER);
            cmd.registerOutParameter(5, Types.TIMESTAMP);
            cmd.executeUpdate();

            int id = cmd.getInt(4);
            Timestamp fecha = cmd.getTimestamp(5);
            if (id <= 0 || fecha == null) {
                throw new SQLException("No se obtuvieron los datos de la auditoria insertada");
            }
            auditoria.setId(id);
            auditoria.setFecha(fecha.toLocalDateTime());
            auditoria.setEstado(EstadoAuditoria.PENDIENTE);
        }
    }
    @Override
    public void update(Auditoria auditoria) throws SQLException {
        validarAuditoriaEscribible(auditoria);
        if (auditoria== null || auditoria.getId()<0) throw new IllegalArgumentException("El id debe ser positivo");

        String sql = "{call modificar_auditoria_atencion(?, ?, ?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt(1, auditoria.getId());
            cmd.setInt(2, auditoria.getResponsable().getId());
            cmd.setString(3, auditoria.getEstado().name());
            cmd.executeUpdate();
        }
    }
    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null || id <= 0) throw new IllegalArgumentException("El id debe ser positivo");
        throw new SQLException("Las auditorias forman parte del historial y no se eliminan");
    }
    private List<Auditoria> mapearAuditorias(ResultSet rs)throws SQLException {
        List<Auditoria> auditorias = new ArrayList<>();
        while (rs.next()) {
            auditorias.add(mapearAuditoria(rs,new Auditoria()));
        }
        return auditorias;
    }
    private Auditoria mapearAuditoria(ResultSet rs,Auditoria auditoria) throws SQLException {
        if (rs.getObject("responsable_id") != null) mapearResponsable(rs,auditoria);
        if (rs.getObject("funcion_id") != null) mapearFuncion(rs,auditoria);
        auditoria.setId(rs.getInt("auditoria_id"));
        auditoria.setFecha( rs.getTimestamp("auditoria_fecha").toLocalDateTime());
        auditoria.setDescripcion(rs.getString("auditoria_descripcion"));
        auditoria.setEstado(EstadoAuditoria.valueOf(rs.getString("auditoria_estado")));
        return auditoria;
    }
    private void mapearResponsable(ResultSet rs,Auditoria auditoria)throws SQLException {
        AdministradorDAO daoAdmin = new AdministradorDAOImpl();
        auditoria.setResponsable(daoAdmin.findById(rs.getInt("administrador_id")));
    }
    private void mapearFuncion(ResultSet rs, Auditoria auditoria) throws SQLException {
        FuncionDAO daoFuncion = new FuncionDAOImpl();
        auditoria.setFuncion(daoFuncion.findById(rs.getInt("funcion_id")));
    }
    private void validarAuditoriaEscribible(Auditoria auditoria) {
        if (auditoria == null) {
            throw new IllegalArgumentException("La auditoria no puede ser nula");
        }
        if (auditoria.getFuncion() == null) {
            throw new IllegalArgumentException(
                    "La funcion de la auditoria no puede ser nula");
        }
        if (auditoria.getResponsable() == null) {
            throw new IllegalArgumentException(
                    "El responsable de la auditoria no puede ser nulo");
        }
    }

}

