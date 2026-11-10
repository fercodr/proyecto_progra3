package pe.edu.pucp.entrefunciones.dao.impl.eventos;
import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
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
                funciones.add(mapear(rs));
            }
            return funciones;
        }
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
