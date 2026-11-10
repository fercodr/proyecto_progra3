package pe.edu.pucp.entrefunciones.dao.impl.auditoria;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import pe.edu.pucp.entrefunciones.dao.auditoria.AuditoriaDAO;
import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
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

public class AuditoriaDAOImpl implements AuditoriaDAO {

    @Override
    public List<Auditoria> findAll() throws SQLException {
        String sql = "{call listar_auditorias()}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            return mapearAuditorias(rs);
        }
    }

    @Override
    public Auditoria findById(Integer id) throws SQLException {
        validarId(id, "auditoria");
        String sql = "{call buscar_auditoria_por_id(?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapearAuditoria(rs) : null;
            }
        }
    }

    @Override
    public List<Auditoria> findByFilters(String descripcion,
                                         EstadoAuditoria estado)
            throws SQLException {
        String sql = "{call listar_auditorias_filtradas(?, ?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {
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
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {
            cmd.setInt(1, auditoria.getFuncion().getId());
            cmd.setInt(2, auditoria.getResponsable().getId());
            cmd.setString(3, auditoria.getDescripcion());
            cmd.registerOutParameter(4, Types.INTEGER);
            cmd.registerOutParameter(5, Types.TIMESTAMP);
            cmd.executeUpdate();

            int id = cmd.getInt(4);
            Timestamp fecha = cmd.getTimestamp(5);
            if (id <= 0 || fecha == null) {
                throw new SQLException(
                        "No se obtuvieron los datos de la auditoria insertada");
            }
            auditoria.setId(id);
            auditoria.setFecha(fecha.toLocalDateTime());
            auditoria.setEstado(EstadoAuditoria.PENDIENTE);
        }
    }

    @Override
    public void update(Auditoria auditoria) throws SQLException {
        validarAuditoriaEscribible(auditoria);
        validarId(auditoria.getId(), "auditoria");
        String sql = "{call modificar_auditoria_atencion(?, ?, ?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {
            cmd.setInt(1, auditoria.getId());
            cmd.setInt(2, auditoria.getResponsable().getId());
            cmd.setString(3, auditoria.getEstado().name());
            cmd.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        validarId(id, "auditoria");
        throw new SQLException(
                "Las auditorias forman parte del historial y no se eliminan");
    }

    @Override
    public List<Administrador> findResponsablesActivos() throws SQLException {
        String sql = "{call listar_responsables_activos_auditoria()}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            List<Administrador> responsables = new ArrayList<>();
            while (rs.next()) {
                responsables.add(mapearResponsable(rs));
            }
            return responsables;
        }
    }

    @Override
    public List<Funcion> findFuncionesAsignables() throws SQLException {
        String sql = "{call listar_funciones_asignables_auditoria()}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            List<Funcion> funciones = new ArrayList<>();
            while (rs.next()) {
                funciones.add(mapearFuncion(rs));
            }
            return funciones;
        }
    }

    @Override
    public Administrador findResponsableById(Integer usuarioId)
            throws SQLException {
        validarId(usuarioId, "administrador");
        String sql = "{call buscar_responsable_auditoria_por_id(?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {
            cmd.setInt(1, usuarioId);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapearResponsable(rs) : null;
            }
        }
    }

    @Override
    public Funcion findFuncionById(Integer funcionId) throws SQLException {
        validarId(funcionId, "funcion");
        String sql = "{call buscar_funcion_auditoria_por_id(?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {
            cmd.setInt(1, funcionId);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapearFuncion(rs) : null;
            }
        }
    }

    private List<Auditoria> mapearAuditorias(ResultSet rs)
            throws SQLException {
        List<Auditoria> auditorias = new ArrayList<>();
        while (rs.next()) {
            auditorias.add(mapearAuditoria(rs));
        }
        return auditorias;
    }

    private Auditoria mapearAuditoria(ResultSet rs) throws SQLException {
        Administrador responsable = null;
        if (rs.getObject("responsable_id") != null) {
            responsable = mapearResponsable(rs);
        }

        Funcion funcion = null;
        if (rs.getObject("funcion_id") != null) {
            funcion = mapearFuncion(rs);
        }

        return Auditoria.restaurarHistorica(
                rs.getInt("auditoria_id"),
                rs.getTimestamp("auditoria_fecha").toLocalDateTime(),
                responsable,
                rs.getString("responsable_texto_original"),
                rs.getString("auditoria_descripcion"),
                funcion,
                EstadoAuditoria.valueOf(rs.getString("auditoria_estado")));
    }

    private Administrador mapearResponsable(ResultSet rs)
            throws SQLException {
        return new Administrador(
                rs.getInt("responsable_id"),
                rs.getString("responsable_nombres"),
                rs.getString("responsable_apellidos"),
                rs.getString("responsable_codigo_empleado"),
                rs.getBoolean("responsable_activo"));
    }

    private Funcion mapearFuncion(ResultSet rs) throws SQLException {
        Evento evento = mapearEvento(rs);
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
        sala.setId(rs.getInt("sala_id"));
        sala.setSede(sede);
        sala.setNombre(rs.getString("sala_nombre"));
        sala.setAforo(rs.getInt("sala_aforo"));
        sala.setTipoSala(rs.getString("tipo_sala"));
        if (rs.getBoolean("sala_activo")) {
            sala.activar();
        } else {
            sala.desactivar();
        }

        Funcion funcion = new Funcion(
                rs.getInt("funcion_id"),
                evento,
                sala,
                rs.getDate("funcion_fecha").toLocalDate(),
                rs.getTime("funcion_hora_inicio").toLocalTime(),
                rs.getTime("funcion_hora_fin").toLocalTime(),
                rs.getString("funcion_idioma"));
        funcion.setEstado(EstadoFuncion.valueOf(
                rs.getString("funcion_estado")));
        evento.agregarFuncion(funcion);
        return funcion;
    }

    private Evento mapearEvento(ResultSet rs) throws SQLException {
        int eventoId = rs.getInt("evento_id");
        double precio = rs.getDouble("evento_precio");
        String tipo = rs.getString("tipo_evento");
        Evento evento = switch (tipo) {
            case "PELICULA" -> new Pelicula(
                    eventoId,
                    precio,
                    rs.getString("nombre_pelicula"),
                    rs.getInt("duracion_minutos"),
                    CategoriaGenero.valueOf(rs.getString("genero")),
                    ClasificacionEdad.valueOf(
                            rs.getString("clasificacion_edad")),
                    rs.getString("director"));
            case "OBRA_TEATRO" -> new ObraTeatro(
                    eventoId,
                    precio,
                    rs.getString("titulo"),
                    rs.getString("nombre_director"),
                    rs.getString("elenco"));
            case "ACTIVIDAD_CULTURAL" -> new ActividadCultural(
                    eventoId,
                    precio,
                    rs.getString("nombre"),
                    rs.getString("expositor"),
                    TipoActividad.valueOf(rs.getString("tipo_actividad")));
            default -> throw new SQLException(
                    "Tipo de evento desconocido para la funcion: " + tipo);
        };

        if (!rs.getBoolean("evento_activo")) {
            evento.desactivar();
        }
        return evento;
    }

    private void validarAuditoriaEscribible(Auditoria auditoria) {
        if (auditoria == null) {
            throw new IllegalArgumentException(
                    "La auditoria no puede ser nula");
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

    private void validarId(Integer id, String entidad) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El id de " + entidad + " debe ser positivo");
        }
    }
}
