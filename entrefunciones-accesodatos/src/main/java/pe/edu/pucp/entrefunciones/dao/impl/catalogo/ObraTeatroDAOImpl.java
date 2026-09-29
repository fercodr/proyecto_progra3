package pe.edu.pucp.entrefunciones.dao.impl.catalogo;

import pe.edu.pucp.entrefunciones.dao.catalogo.ObraTeatroDAO;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.catalogo.CategoriaGenero;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ClasificacionEdad;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ObraTeatro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ObraTeatroDAOImpl extends RegistroDAOImpl<ObraTeatro> implements ObraTeatroDAO {

    @Override
    public List<ObraTeatro> findAll() throws SQLException {
        String sql = "{call listar_obras_teatro()}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            List<ObraTeatro> obras = new ArrayList<>();
            while (rs.next()) {
                obras.add(mapear(rs, new ObraTeatro()));
            }
            return obras;
        }
    }

    @Override
    public ObraTeatro findById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        String sql = "{call buscar_obra_teatro_por_id(?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new ObraTeatro()) : null;
            }
        }
    }

    @Override
    public void insert(ObraTeatro obraTeatro) throws SQLException {
        if (obraTeatro == null) {
            throw new IllegalArgumentException("La obra de teatro no puede ser nula");
        }
        String sql = "{call insertar_obra_teatro(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", obraTeatro.getNombre());
            cmd.setString("p_genero", obraTeatro.getGenero() != null ? obraTeatro.getGenero().name() : null);
            cmd.setString("p_clasificacion_edad", obraTeatro.getClasificacionEdad() != null ? obraTeatro.getClasificacionEdad().name() : null);
            cmd.setString("p_director", obraTeatro.getDirector());
            cmd.setString("p_elenco", obraTeatro.getElenco());
            cmd.setInt("p_duracion_minutos", obraTeatro.getDuracionMinutos());
            cmd.setBoolean("p_activo", obraTeatro.isActivo());
            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la obra de teatro");
            }
            obraTeatro.setId(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(ObraTeatro obraTeatro) throws SQLException {
        if (obraTeatro == null) {
            throw new IllegalArgumentException("La obra de teatro no puede ser nula");
        }
        String sql = "{call modificar_obra_teatro(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", obraTeatro.getNombre());
            cmd.setString("p_genero", obraTeatro.getGenero() != null ? obraTeatro.getGenero().name() : null);
            cmd.setString("p_clasificacion_edad", obraTeatro.getClasificacionEdad() != null ? obraTeatro.getClasificacionEdad().name() : null);
            cmd.setString("p_director", obraTeatro.getDirector());
            cmd.setString("p_elenco", obraTeatro.getElenco());
            cmd.setInt("p_duracion_minutos", obraTeatro.getDuracionMinutos());
            cmd.setBoolean("p_activo", obraTeatro.isActivo());
            cmd.setInt("p_id", obraTeatro.getId());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo actualizar la obra de teatro");
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        String sql = "{call eliminar_obra_teatro(?)}";
        try (Connection conn = DBManager.getInstance().getConnection();
             CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", id);
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar la obra de teatro");
            }
        }
    }

    @Override
    protected ObraTeatro mapear(ResultSet rs, ObraTeatro obraTeatro) throws SQLException {
        super.mapear(rs, obraTeatro);
        obraTeatro.setNombre(rs.getString("nombre"));
        if (rs.getString("genero") != null) {
            obraTeatro.setGenero(Enum.valueOf(CategoriaGenero.class, rs.getString("genero")));
        }
        if (rs.getString("clasificacion_edad") != null) {
            obraTeatro.setClasificacionEdad(Enum.valueOf(ClasificacionEdad.class, rs.getString("clasificacion_edad")));
        }
        obraTeatro.setDirector(rs.getString("director"));
        obraTeatro.setElenco(rs.getString("elenco"));
        obraTeatro.setDuracionMinutos(rs.getInt("duracion_minutos"));
        return obraTeatro;
    }
}