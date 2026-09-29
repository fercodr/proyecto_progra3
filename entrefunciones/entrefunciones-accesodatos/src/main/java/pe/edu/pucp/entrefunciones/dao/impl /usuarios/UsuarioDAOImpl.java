package pe.edu.pucp.accesodatos.dao.impl.usuarios;

import pe.edu.pucp.accesodatos.dao.usuarios.UsuarioDAO;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Usuario;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public abstract class UsuarioDAOImpl<T extends Usuario>
        implements UsuarioDAO<T> {

    @Override
    public List<T> findAll() throws SQLException {

        String sql = getProcedimientoListar();

        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql);
                ResultSet rs = cmd.executeQuery()
        ) {

            List<T> usuarios = new ArrayList<>();

            while (rs.next()) {
                usuarios.add(construir(rs));
            }

            return usuarios;
        }
    }

    @Override
    public T findById(Integer id) throws SQLException {

        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = getProcedimientoBuscarPorId();

        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)
        ) {

            cmd.setInt("p_id", id);

            try (ResultSet rs = cmd.executeQuery()) {
                if (rs.next()) {
                    return construir(rs);
                }

                return null;
            }
        }
    }

    @Override
    public T findByCorreo(String correo) throws SQLException {

        if (correo == null) {
            throw new IllegalArgumentException(
                    "El correo no puede ser nulo"
            );
        }

        String sql = "{call buscar_usuario_por_correo(?)}";

        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)
        ) {

            cmd.setString("p_correo", correo);

            try (ResultSet rs = cmd.executeQuery()) {

                if (rs.next()) {
                    return construir(rs);
                }

                return null;
            }
        }
    }

    @Override
    public T findByDni(String dni) throws SQLException {

        if (dni == null) {
            throw new IllegalArgumentException(
                    "El DNI no puede ser nulo"
            );
        }

        String sql = "{call buscar_usuario_por_dni(?)}";

        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)
        ) {

            cmd.setString("p_dni", dni);

            try (ResultSet rs = cmd.executeQuery()) {

                if (rs.next()) {
                    return construir(rs);
                }

                return null;
            }
        }
    }

    @Override
    public void insert(T usuario) throws SQLException {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario no puede ser nulo"
            );
        }

        String sql = getProcedimientoInsertar();

        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)
        ) {

            cmd.setString("p_nombres", usuario.getNombres());
            cmd.setString("p_apellidos", usuario.getApellidos());
            cmd.setString("p_correo", usuario.getCorreo());
            cmd.setString("p_contrasena", usuario.getContrasena());
            cmd.setString("p_dni", usuario.getDni());
            cmd.setBoolean("p_activo", usuario.estaActivo());

            completarInsert(cmd, usuario);

            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo insertar el usuario"
                );
            }

            usuario.setId(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(T usuario) throws SQLException {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario no puede ser nulo"
            );
        }

        String sql = getProcedimientoModificar();

        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)
        ) {

            cmd.setString("p_nombres", usuario.getNombres());
            cmd.setString("p_apellidos", usuario.getApellidos());
            cmd.setString("p_correo", usuario.getCorreo());
            cmd.setString("p_contrasena", usuario.getContrasena());
            cmd.setString("p_dni", usuario.getDni());
            cmd.setBoolean("p_activo", usuario.estaActivo());

            completarUpdate(cmd, usuario);

            cmd.setInt("p_id", usuario.getId());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo actualizar el usuario"
                );
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El id no puede ser nulo"
            );
        }

        String sql = getProcedimientoEliminar();

        try (
                Connection conn = DBManager.getInstance().getConnection();
                CallableStatement cmd = conn.prepareCall(sql)
        ) {

            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo eliminar el usuario"
                );
            }
        }
    }

    protected abstract T construir(ResultSet rs) throws SQLException;

    protected abstract String getProcedimientoListar();

    protected abstract String getProcedimientoBuscarPorId();

    protected abstract String getProcedimientoInsertar();

    protected abstract String getProcedimientoModificar();

    protected abstract String getProcedimientoEliminar();

    protected abstract void completarInsert(
            CallableStatement cmd,
            T usuario
    ) throws SQLException;

    protected abstract void completarUpdate(
            CallableStatement cmd,
            T usuario
    ) throws SQLException;
}
