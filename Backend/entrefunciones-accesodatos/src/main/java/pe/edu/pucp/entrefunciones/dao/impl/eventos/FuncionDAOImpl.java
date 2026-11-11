package pe.edu.pucp.entrefunciones.dao.impl.eventos;
import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ActividadCultural;
import pe.edu.pucp.entrefunciones.modelo.catalogo.CategoriaGenero;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ClasificacionEdad;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ObraTeatro;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Pelicula;
import pe.edu.pucp.entrefunciones.modelo.catalogo.TipoActividad;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
public class FuncionDAOImpl implements FuncionDAO {
    public List<Funcion> findAllActiveFunctions() throws SQLException {
        String sql = "{call listar_funciones_asignables_auditoria()}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            List<Funcion> funciones = new ArrayList<>();
            while (rs.next()) {
                funciones.add(mapearAsignable(rs));
            }
            return funciones;
        }
    }

    private Funcion mapearAsignable(ResultSet rs) throws SQLException {
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

        Evento evento = construirEventoAsignable(rs);
        if (!rs.getBoolean("evento_activo")) {
            evento.desactivar();
        }

        Funcion funcion = new Funcion(
                rs.getInt("funcion_id"),
                evento,
                sala,
                rs.getDate("funcion_fecha").toLocalDate(),
                rs.getTime("funcion_hora_inicio").toLocalTime(),
                rs.getTime("funcion_hora_fin").toLocalTime(),
                rs.getString("funcion_idioma"));
        funcion.setEstado(Enum.valueOf(
                EstadoFuncion.class,
                rs.getString("funcion_estado")));
        evento.agregarFuncion(funcion);
        sala.agregarFuncion(funcion);
        sede.agregarSala(sala);
        return funcion;
    }

    private Evento construirEventoAsignable(ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo_evento");
        double precio = rs.getDouble("evento_precio");
        int id = rs.getInt("evento_id");
        return switch (tipo) {
            case "PELICULA" -> new Pelicula(
                    id, precio,
                    rs.getString("nombre_pelicula"),
                    rs.getInt("duracion_minutos"),
                    Enum.valueOf(CategoriaGenero.class, rs.getString("genero")),
                    Enum.valueOf(ClasificacionEdad.class,
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
                    Enum.valueOf(TipoActividad.class,
                            rs.getString("tipo_actividad")));
            default -> throw new SQLException(
                    "Tipo de evento desconocido: " + tipo);
        };
    }

    @Override
    public void insertFunciones(int idEvento, List<Funcion> funciones) throws SQLException {
        Connection connection = TransactionsManager.getConnection();

        for (Funcion funcion : funciones) {
            insertar(connection, idEvento, funcion);
        }
    }

    @Override
    public void updateFunciones(int idEvento, List<Funcion> funciones) throws SQLException {
        Connection connection = TransactionsManager.getConnection();

        for (Funcion funcion : funciones) {
            if (funcion.getId() == 0) {
                insertar(connection, idEvento, funcion);
            } else {
                modificar(connection, funcion);
            }
        }
    }

    @Override
    public void deleteFunciones(int idEvento) throws SQLException {
        Connection connection = TransactionsManager.getConnection();

        String sql = "{call cancelar_funciones_por_evento(?)}";
        try (CallableStatement cmd = connection.prepareCall(sql)) {
            cmd.setInt("p_evento_id", idEvento);
            cmd.executeUpdate();
        }
    }

    @Override
    public List<Funcion> findByEvento(Evento evento) throws SQLException {
        String sql = "{call listar_funciones_por_evento(?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {

            cmd.setInt("p_evento_id", evento.getId());
            try (ResultSet rs = cmd.executeQuery()) {
                List<Funcion> funciones = new ArrayList<>();
                while (rs.next()) {
                    funciones.add(mapear(rs, evento));
                }
                return funciones;
            }
        }
    }
    @Override
    public List<Funcion> findAll() throws SQLException {
        String sql = "{call listar_funciones()}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {

            List<Funcion> funciones = new ArrayList<>();
            while (rs.next()) {
                funciones.add(mapear(rs));
            }
            return funciones;
        }
    }

    @Override
    public List<Funcion> findBySalaId(int idSala) throws SQLException {
        String sql = "{call listar_funciones_por_sala(?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {

            cmd.setInt("p_sala_id", idSala);
            try (ResultSet rs = cmd.executeQuery()) {
                List<Funcion> funciones = new ArrayList<>();
                while (rs.next()) {
                    funciones.add(mapear(rs));
                }
                return funciones;
            }
        }
    }

    @Override
    public List<Funcion> findReportes(LocalDate desde, LocalDate hasta,
                                      Integer idSede, Integer idSala)
            throws SQLException {
        String sql = "{call listar_reportes_funciones(?, ?, ?, ?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {
            cmd.setDate("p_desde", Date.valueOf(desde));
            cmd.setDate("p_hasta", Date.valueOf(hasta));
            if (idSede == null) {
                cmd.setNull("p_sede_id", Types.INTEGER);
            } else {
                cmd.setInt("p_sede_id", idSede);
            }
            if (idSala == null) {
                cmd.setNull("p_sala_id", Types.INTEGER);
            } else {
                cmd.setInt("p_sala_id", idSala);
            }

            try (ResultSet rs = cmd.executeQuery()) {
                List<Funcion> funciones = new ArrayList<>();
                while (rs.next()) {
                    funciones.add(mapearReporte(rs));
                }
                return funciones;
            }
        }
    }

    private Funcion mapearReporte(ResultSet rs) throws SQLException {
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
                rs.getString("sala_tipo_sala"));
        if (!rs.getBoolean("sala_activo")) {
            sala.desactivar();
        }

        Evento evento = construirEventoAsignable(rs);
        if (!rs.getBoolean("evento_activo")) {
            evento.desactivar();
        }

        Funcion funcion = new Funcion(
                rs.getInt("funcion_id"),
                evento,
                sala,
                rs.getDate("funcion_fecha").toLocalDate(),
                rs.getTime("funcion_hora_inicio").toLocalTime(),
                rs.getTime("funcion_hora_fin").toLocalTime(),
                rs.getString("funcion_idioma"));
        funcion.setEstado(Enum.valueOf(
                EstadoFuncion.class, rs.getString("funcion_estado")));
        funcion.setEntradasNoCanceladas(rs.getInt("entradas_no_canceladas"));
        funcion.setEntradasCanceladas(rs.getInt("entradas_canceladas"));
        BigDecimal ingresos = rs.getBigDecimal("ingresos_validos");
        funcion.setIngresosValidos(ingresos == null ? BigDecimal.ZERO : ingresos);
        funcion.setAsistentes((Integer) rs.getObject("asistentes"));
        funcion.setPorcentajeOcupacion(
                rs.getBigDecimal("porcentaje_ocupacion"));

        evento.agregarFuncion(funcion);
        sala.agregarFuncion(funcion);
        sede.agregarSala(sala);
        return funcion;
    }

    @Override
    public Funcion findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_funcion_por_id(?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {

            cmd.setInt("p_id", id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insert(Funcion funcion) throws SQLException {
        if (funcion == null) {
            throw new IllegalArgumentException("La función no puede ser nula");
        }

        try (ConnectionScope scope = ConnectionScope.openForRead();) {
            insertar(scope.connection(), funcion.getEvento().getId(), funcion);
        }
    }

    @Override
    public void update(Funcion funcion) throws SQLException {
        if (funcion == null) {
            throw new IllegalArgumentException("La función no puede ser nula");
        }

        try (ConnectionScope scope = ConnectionScope.openForRead();) {
            modificar(scope.connection(), funcion);
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call cancelar_funcion(?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {

            cmd.setInt("p_id", id);
            cmd.executeUpdate();
        }
    }
    private void insertar(Connection connection, int idEvento, Funcion funcion) throws SQLException {
        String sql = "{call insertar_funcion(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = connection.prepareCall(sql)) {
            cmd.setInt("p_evento_id", idEvento);
            cmd.setInt("p_sala_id", funcion.getSala().getId());
            cmd.setDate("p_fecha", Date.valueOf(funcion.getFecha()));
            cmd.setTime("p_hora_inicio", Time.valueOf(funcion.getHoraInicio()));
            cmd.setTime("p_hora_fin", Time.valueOf(funcion.getHoraFin()));
            cmd.setString("p_idioma", funcion.getIdioma());
            cmd.setString("p_estado", funcion.getEstado().name());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la función");
            }

            funcion.setId(cmd.getInt("p_id"));
        }
    }

    private void modificar(Connection connection, Funcion funcion) throws SQLException {
        String sql = "{call modificar_funcion(?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = connection.prepareCall(sql)) {
            cmd.setInt("p_id", funcion.getId());
            cmd.setInt("p_sala_id", funcion.getSala().getId());
            cmd.setDate("p_fecha", Date.valueOf(funcion.getFecha()));
            cmd.setTime("p_hora_inicio", Time.valueOf(funcion.getHoraInicio()));
            cmd.setTime("p_hora_fin", Time.valueOf(funcion.getHoraFin()));
            cmd.setString("p_idioma", funcion.getIdioma());
            cmd.setString("p_estado", funcion.getEstado().name());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la función");
            }
        }
    }
    private Funcion mapear(ResultSet rs, Evento evento) throws SQLException {
        Funcion funcion = new Funcion(
                rs.getInt("id"),
                evento,
                new SalaDAOImpl().findById(rs.getInt("sala_id")),
                rs.getDate("fecha").toLocalDate(),
                rs.getTime("hora_inicio").toLocalTime(),
                rs.getTime("hora_fin").toLocalTime(),
                rs.getString("idioma"));

        funcion.setEstado(Enum.valueOf(EstadoFuncion.class, rs.getString("estado")));

        return funcion;
    }

    private Funcion mapear(ResultSet rs) throws SQLException {
        Evento evento = new EventoDAOImpl().findByIdSinFunciones(rs.getInt("evento_id"));
        if (evento == null) {
            throw new SQLException("La función " + rs.getInt("id") + " no tiene evento");
        }

        Funcion funcion = mapear(rs, evento);
        evento.agregarFuncion(funcion);
        return funcion;
    }
}
