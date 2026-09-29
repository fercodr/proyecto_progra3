package pe.edu.pucp.entrefunciones.dao.impl.catalogo;

import pe.edu.pucp.entrefunciones.dao.PeliculaDAO;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.catalogo.CategoriaGenero;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ClasificacionEdad;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Pelicula;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeliculaDAOImpl implements PeliculaDAO {

    @Override
    public List<Pelicula> findAll() throws SQLException {
        String sql = "{call listar_peliculas()}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            List<Pelicula> peliculas = new ArrayList<>();
            while (rs.next()) {
                peliculas.add(mapear(rs));
            }
            return peliculas;
        }
    }

    @Override
    public Pelicula findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        String sql = "{call buscar_pelicula_por_id(?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insert(Pelicula pelicula) throws SQLException {
        if (pelicula == null) {
            throw new IllegalArgumentException("La pelicula no puede ser nula");
        }
        String sql = "{call insertar_pelicula(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", pelicula.getNombre());
            cmd.setString("p_genero", pelicula.getGenero() != null ? pelicula.getGenero().name() : null);
            cmd.setString("p_clasificacion_edad", pelicula.getClasificacionEdad() != null ? pelicula.getClasificacionEdad().name() : null);
            cmd.setString("p_director", pelicula.getDirector());
            cmd.setInt("p_duracion_minutos", pelicula.getDuracionMinutos());
            cmd.setBoolean("p_activo", pelicula.isActivo());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la pelicula");
            }
            pelicula.setId(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(Pelicula pelicula) throws SQLException {
        if (pelicula == null) {
            throw new IllegalArgumentException("La pelicula no puede ser nula");
        }
        String sql = "{call modificar_pelicula(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", pelicula.getNombre());
            cmd.setString("p_genero", pelicula.getGenero() != null ? pelicula.getGenero().name() : null);
            cmd.setString("p_clasificacion_edad", pelicula.getClasificacionEdad() != null ? pelicula.getClasificacionEdad().name() : null);
            cmd.setString("p_director", pelicula.getDirector());
            cmd.setInt("p_duracion_minutos", pelicula.getDuracionMinutos());
            cmd.setBoolean("p_activo", pelicula.isActivo());
            cmd.setInt("p_id", pelicula.getId());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la pelicula");
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        String sql = "{call eliminar_pelicula(?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar la pelicula");
            }
        }
    }

    private Pelicula mapear(ResultSet rs) throws SQLException {
        Pelicula pelicula = new Pelicula();
        pelicula.setId(rs.getInt("id"));
        pelicula.setNombre(rs.getString("nombre_pelicula"));
        if (rs.getString("genero") != null) {
            pelicula.setGenero(Enum.valueOf(CategoriaGenero.class, rs.getString("genero")));
        }
        if (rs.getString("clasificacion_edad") != null) {
            pelicula.setClasificacionEdad(Enum.valueOf(ClasificacionEdad.class, rs.getString("clasificacion_edad")));
        }
        pelicula.setDirector(rs.getString("director"));
        pelicula.setDuracionMinutos(rs.getInt("duracion_minutos"));
        pelicula.setActivo(rs.getBoolean("activo"));
        return pelicula;
    }
}
