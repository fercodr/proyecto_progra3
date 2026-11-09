package pe.edu.pucp.entrefunciones.dao.impl.eventos;
import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ActividadCultural;
import pe.edu.pucp.entrefunciones.modelo.catalogo.CategoriaGenero;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ClasificacionEdad;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ObraTeatro;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Pelicula;
import pe.edu.pucp.entrefunciones.modelo.catalogo.TipoActividad;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
public class EventoDAOImpl implements EventoDAO {

    private final FuncionDAO funcionDAO = new FuncionDAOImpl();

    @FunctionalInterface
    private interface Operacion {
        void ejecutar() throws SQLException;
    }

    @Override
    public List<Evento> findAll() throws SQLException {
        String sql = "{call listar_eventos()}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {

            List<Evento> eventos = new ArrayList<>();
            while (rs.next()) {
                eventos.add(mapear(rs));
            }
            return eventos;
        }
    }

    @Override
    public Evento findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call buscar_evento_por_id(?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {

            cmd.setInt("p_id", id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    Evento findByIdSinFunciones(int id) throws SQLException {
        String sql = "{call buscar_evento_por_id(?)}";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             CallableStatement cmd = scope.connection().prepareCall(sql)) {

            cmd.setInt("p_id", id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? construir(rs) : null;
            }
        }
    }
    @Override
    public void insert(Evento evento) throws SQLException {
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo");
        }

        enTransaccion(() -> {
            insertarBase(evento);
            insertarSubtipo(evento);
            funcionDAO.insertFunciones(evento.getId(), evento.getFunciones());
        });
    }

    @Override
    public void update(Evento evento) throws SQLException {
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo");
        }

        enTransaccion(() -> {
            modificarBase(evento);
            modificarSubtipo(evento);

            funcionDAO.updateFunciones(evento.getId(), evento.getFunciones());
        });
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        enTransaccion(() -> {
            Connection connection = TransactionsManager.getConnection();

            String sql = "{call eliminar_evento(?)}";
            try (CallableStatement cmd = connection.prepareCall(sql)) {
                cmd.setInt("p_id", id);
                if (cmd.executeUpdate() == 0) {
                    throw new SQLException("No se pudo eliminar el evento");
                }
            }
            // Eliminado logico del evento -> sus funciones se cancelan.
            funcionDAO.deleteFunciones(id);
        });
    }

    private void enTransaccion(Operacion operacion) throws SQLException {
        boolean duenio = !TransactionsManager.activa();
        if (duenio) {
            TransactionsManager.iniciar();
        }

        try {
            operacion.ejecutar();
            if (duenio) {
                TransactionsManager.commit();
            }
        } catch (SQLException | RuntimeException ex) {
            if (duenio && TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw ex;
        }
    }

    private void insertarBase(Evento evento) throws SQLException {
        Connection connection = TransactionsManager.getConnection();

        String sql = "{call insertar_evento(?, ?, ?, ?)}";
        try (CallableStatement cmd = connection.prepareCall(sql)) {
            cmd.setDouble("p_precio", evento.getPrecio());
            cmd.setBoolean("p_activo", evento.estaActivo());
            cmd.setString("p_imagen_url", evento.getImagenUrl());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar el evento");
            }

            evento.setId(cmd.getInt("p_id"));
        }
    }

    private void modificarBase(Evento evento) throws SQLException {
        Connection connection = TransactionsManager.getConnection();

        String sql = "{call modificar_evento(?, ?, ?, ?)}";
        try (CallableStatement cmd = connection.prepareCall(sql)) {
            cmd.setInt("p_id", evento.getId());
            cmd.setDouble("p_precio", evento.getPrecio());
            cmd.setBoolean("p_activo", evento.estaActivo());
            cmd.setString("p_imagen_url", evento.getImagenUrl());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar el evento");
            }
        }
    }
    private void insertarSubtipo(Evento evento) throws SQLException {
        Connection connection = TransactionsManager.getConnection();

        if (evento instanceof Pelicula pelicula) {
            String sql = "{call insertar_evento_pelicula(?, ?, ?, ?, ?, ?)}";
            try (CallableStatement cmd = connection.prepareCall(sql)) {
                cmd.setInt("p_evento_id", pelicula.getId());
                cmd.setString("p_nombre_pelicula", pelicula.getNombrePelicula());
                cmd.setInt("p_duracion_minutos", pelicula.getDuracionMinutos());
                cmd.setString("p_genero", pelicula.getGenero().name());
                cmd.setString("p_clasificacion_edad", pelicula.getClasificacionPorEdad().name());
                cmd.setString("p_director", pelicula.getDirector());
                ejecutar(cmd, "No se pudo insertar la película");
            }
        } else if (evento instanceof ObraTeatro obra) {
            String sql = "{call insertar_evento_obra_teatro(?, ?, ?, ?)}";
            try (CallableStatement cmd = connection.prepareCall(sql)) {
                cmd.setInt("p_evento_id", obra.getId());
                cmd.setString("p_titulo", obra.getTitulo());
                cmd.setString("p_nombre_director", obra.getNombreDirector());
                cmd.setString("p_elenco", obra.getElenco());
                ejecutar(cmd, "No se pudo insertar la obra de teatro");
            }
        } else if (evento instanceof ActividadCultural actividad) {
            String sql = "{call insertar_evento_actividad_cultural(?, ?, ?, ?)}";
            try (CallableStatement cmd = connection.prepareCall(sql)) {
                cmd.setInt("p_evento_id", actividad.getId());
                cmd.setString("p_nombre", actividad.getNombre());
                cmd.setString("p_expositor", actividad.getExpositor());
                cmd.setString("p_tipo_actividad", actividad.getTipoActividad().name());
                ejecutar(cmd, "No se pudo insertar la actividad cultural");
            }
        } else {
            throw new IllegalArgumentException("Tipo de evento no soportado: " + evento.getClass().getSimpleName());
        }
    }

    private void modificarSubtipo(Evento evento) throws SQLException {
        Connection connection = TransactionsManager.getConnection();

        if (evento instanceof Pelicula pelicula) {
            String sql = "{call modificar_evento_pelicula(?, ?, ?, ?, ?, ?)}";
            try (CallableStatement cmd = connection.prepareCall(sql)) {
                cmd.setInt("p_evento_id", pelicula.getId());
                cmd.setString("p_nombre_pelicula", pelicula.getNombrePelicula());
                cmd.setInt("p_duracion_minutos", pelicula.getDuracionMinutos());
                cmd.setString("p_genero", pelicula.getGenero().name());
                cmd.setString("p_clasificacion_edad", pelicula.getClasificacionPorEdad().name());
                cmd.setString("p_director", pelicula.getDirector());
                ejecutar(cmd, "No se pudo actualizar la película");
            }
        } else if (evento instanceof ObraTeatro obra) {
            String sql = "{call modificar_evento_obra_teatro(?, ?, ?, ?)}";
            try (CallableStatement cmd = connection.prepareCall(sql)) {
                cmd.setInt("p_evento_id", obra.getId());
                cmd.setString("p_titulo", obra.getTitulo());
                cmd.setString("p_nombre_director", obra.getNombreDirector());
                cmd.setString("p_elenco", obra.getElenco());
                ejecutar(cmd, "No se pudo actualizar la obra de teatro");
            }
        } else if (evento instanceof ActividadCultural actividad) {
            String sql = "{call modificar_evento_actividad_cultural(?, ?, ?, ?)}";
            try (CallableStatement cmd = connection.prepareCall(sql)) {
                cmd.setInt("p_evento_id", actividad.getId());
                cmd.setString("p_nombre", actividad.getNombre());
                cmd.setString("p_expositor", actividad.getExpositor());
                cmd.setString("p_tipo_actividad", actividad.getTipoActividad().name());
                ejecutar(cmd, "No se pudo actualizar la actividad cultural");
            }
        } else {
            throw new IllegalArgumentException("Tipo de evento no soportado: " + evento.getClass().getSimpleName());
        }
    }

    private void ejecutar(CallableStatement cmd, String mensajeError) throws SQLException {
        if (cmd.executeUpdate() == 0) {
            throw new SQLException(mensajeError);
        }
    }
    private Evento mapear(ResultSet rs) throws SQLException {
        Evento evento = construir(rs);

        for (Funcion funcion : funcionDAO.findByEvento(evento)) {
            evento.agregarFuncion(funcion);
        }

        return evento;
    }

    private Evento construir(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        double precio = rs.getDouble("precio");
        String tipo = rs.getString("tipo_evento");

        if (tipo == null) {
            throw new SQLException("El evento " + id + " no tiene subtipo registrado");
        }

        Evento evento = switch (tipo) {
            case "PELICULA" -> new Pelicula(id, precio,
                    rs.getString("nombre_pelicula"),
                    rs.getInt("duracion_minutos"),
                    Enum.valueOf(CategoriaGenero.class, rs.getString("genero")),
                    Enum.valueOf(ClasificacionEdad.class, rs.getString("clasificacion_edad")),
                    rs.getString("director"));
            case "OBRA_TEATRO" -> new ObraTeatro(id, precio,
                    rs.getString("titulo"),
                    rs.getString("nombre_director"),
                    rs.getString("elenco"));
            case "ACTIVIDAD_CULTURAL" -> new ActividadCultural(id, precio,
                    rs.getString("nombre"),
                    rs.getString("expositor"),
                    Enum.valueOf(TipoActividad.class, rs.getString("tipo_actividad")));
            default -> throw new SQLException("Tipo de evento desconocido: " + tipo);
        };
        evento.setImagenUrl(rs.getString("imagen_url"));

        if (!rs.getBoolean("activo")) {
            evento.desactivar();
        }
        return evento;
    }
}

