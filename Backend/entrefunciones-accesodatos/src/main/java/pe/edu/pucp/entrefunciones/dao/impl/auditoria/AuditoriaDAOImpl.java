package pe.edu.pucp.entrefunciones.dao.impl.auditoria;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.auditoria.Auditoria;
import pe.edu.pucp.entrefunciones.modelo.auditoria.EstadoAuditoria;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ActividadCultural;
import pe.edu.pucp.entrefunciones.modelo.catalogo.CategoriaGenero;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ClasificacionEdad;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ObraTeatro;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Pelicula;
import pe.edu.pucp.entrefunciones.modelo.catalogo.TipoActividad;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;
import pe.edu.pucp.entrefunciones.dao.auditoria.AuditoriaDAO;


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
        if (rs.getObject("responsable_id") != null) {
            auditoria.setResponsable(mapearResponsable(rs));
        }
        if (rs.getObject("funcion_id") != null) {
            auditoria.setFuncion(mapearFuncion(rs));
        }
        auditoria.setId(rs.getInt("auditoria_id"));
        auditoria.setFecha( rs.getTimestamp("auditoria_fecha").toLocalDateTime());
        auditoria.setDescripcion(rs.getString("auditoria_descripcion"));
        auditoria.setResponsableTextoOriginal(
                rs.getString("responsable_texto_original"));
        auditoria.setEstado(EstadoAuditoria.valueOf(rs.getString("auditoria_estado")));
        return auditoria;
    }

    private Administrador mapearResponsable(ResultSet rs) throws SQLException {
        Administrador administrador = new Administrador(
                rs.getInt("responsable_id"),
                rs.getString("responsable_nombres"),
                rs.getString("responsable_apellidos"),
                rs.getString("responsable_codigo_empleado"),
                rs.getBoolean("responsable_activo"));
        if (!rs.getBoolean("responsable_activo")) {
            administrador.desactivar();
        }
        return administrador;
    }

    private Funcion mapearFuncion(ResultSet rs) throws SQLException {
        Sede sede = new Sede(
                rs.getInt("sede_id"),
                rs.getString("sede_razon_social"),
                rs.getString("sede_direccion"),
                rs.getString("sede_ciudad"),
                rs.getString("sede_telefono_contacto"));
        if (!rs.getBoolean("sede_activo")) {
            sede.desactivar();
        }

        Sala sala = new Sala(
                rs.getInt("sala_id"),
                sede,
                rs.getString("sala_nombre"),
                rs.getInt("sala_aforo"),
                rs.getString("tipo_sala"));
        if (!rs.getBoolean("sala_activo")) {
            sala.desactivar();
        }

        Evento evento = mapearEvento(rs);
        Funcion funcion = new Funcion(
                rs.getInt("funcion_id"),
                evento,
                sala,
                rs.getDate("funcion_fecha").toLocalDate(),
                rs.getTime("funcion_hora_inicio").toLocalTime(),
                rs.getTime("funcion_hora_fin").toLocalTime(),
                rs.getString("funcion_idioma"));
        funcion.setEstado(EstadoFuncion.valueOf(rs.getString("funcion_estado")));
        evento.agregarFuncion(funcion);
        sala.agregarFuncion(funcion);
        return funcion;
    }

    private Evento mapearEvento(ResultSet rs) throws SQLException {
        int id = rs.getInt("evento_id");
        double precio = rs.getDouble("evento_precio");
        String tipo = rs.getString("tipo_evento");
        Evento evento;
        try {
            evento = switch (tipo) {
                case "PELICULA" -> new Pelicula(
                        id, precio,
                        rs.getString("nombre_pelicula"),
                        rs.getInt("duracion_minutos"),
                        CategoriaGenero.valueOf(rs.getString("genero")),
                        ClasificacionEdad.valueOf(
                                rs.getString("clasificacion_edad")),
                        rs.getString("director"));
                case "OBRA_TEATRO" -> new ObraTeatro(
                        id, precio,
                        rs.getString("titulo"),
                        rs.getString("nombre_director"),
                        rs.getString("elenco"));
                case "ACTIVIDAD_CULTURAL" -> new ActividadCultural(
                        id, precio,
                        rs.getString("nombre"),
                        rs.getString("expositor"),
                        TipoActividad.valueOf(rs.getString("tipo_actividad")));
                default -> throw new SQLException(
                        "Tipo de evento no reconocido: " + tipo);
            };
        } catch (IllegalArgumentException ex) {
            throw new SQLException("Datos de evento invalidos", ex);
        }
        if (!rs.getBoolean("evento_activo")) {
            evento.desactivar();
        }
        return evento;
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

