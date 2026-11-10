package pe.edu.pucp.entrefunciones.dao.impl.usuarios;

import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.dao.usuarios.UsuarioDAO;
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

    protected abstract T construir(ResultSet rs) throws SQLException;

    protected abstract String getProcedimientoListar();

    protected abstract String getProcedimientoBuscarPorId();

    protected abstract String getProcedimientoBuscarPorCorreo();

    protected abstract String getProcedimientoBuscarPorDni();

    protected abstract String getProcedimientoInsertar();

    protected abstract String getProcedimientoModificar();

    protected abstract String getProcedimientoEliminar();

    @Override
    public List<T> findAll() throws SQLException {

        String sql = "{call " + getProcedimientoListar() + "()}";

        try (
            ConnectionScope scope = ConnectionScope.openForRead();
            CallableStatement cmd = scope.connection().prepareCall(sql);
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

        String sql = "{call " + getProcedimientoBuscarPorId() + "(?)}";

        try (
            ConnectionScope scope = ConnectionScope.openForRead();
            CallableStatement cmd = scope.connection().prepareCall(sql)
        ) {

            cmd.setInt("p_id", id);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? construir(rs) : null;
            }
        }
    }

    @Override
    public T findByCorreo(String correo) throws SQLException {

        if (correo == null) {
            throw new IllegalArgumentException("El correo no puede ser nulo");
        }

        String sql = "{call " + getProcedimientoBuscarPorCorreo() + "(?)}";

        try (
            ConnectionScope scope = ConnectionScope.openForRead();
            CallableStatement cmd = scope.connection().prepareCall(sql)
        ) {

            cmd.setString("p_correo", correo);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? construir(rs) : null;
            }
        }
    }

    @Override
    public T findByDni(String dni) throws SQLException {

        if (dni == null) {
            throw new IllegalArgumentException("El DNI no puede ser nulo");
        }

        String sql = "{call " + getProcedimientoBuscarPorDni() + "(?)}";

        try (
            ConnectionScope scope = ConnectionScope.openForRead();
            CallableStatement cmd = scope.connection().prepareCall(sql)
        ) {

            cmd.setString("p_dni", dni);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? construir(rs) : null;
            }
        }
    }

    @Override
    public void insert(T usuario) throws SQLException {

        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        String sql = "{call " + getProcedimientoInsertar()
                + "(?, ?, ?, ?, ?, ?, ?)}";

        try (
            ConnectionScope scope = ConnectionScope.openForRead();
            CallableStatement cmd = scope.connection().prepareCall(sql)
        ) {

            cmd.setString("p_nombres", usuario.getNombres());
            cmd.setString("p_apellidos", usuario.getApellidos());
            cmd.setString("p_correo", usuario.getCorreo());
            cmd.setString("p_contrasena", usuario.getContrasena());
            cmd.setString("p_dni", usuario.getDni());
            cmd.setBoolean("p_activo", usuario.estaActivo());

            cmd.registerOutParameter("p_id", Types.INTEGER);

            cmd.executeUpdate();

            usuario.setId(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(T usuario) throws SQLException {

        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        String sql = "{call " + getProcedimientoModificar()
                + "(?, ?, ?, ?, ?, ?, ?)}";

        try (
            ConnectionScope scope = ConnectionScope.openForRead();
            CallableStatement cmd = scope.connection().prepareCall(sql)
        ) {

            cmd.setString("p_nombres", usuario.getNombres());
            cmd.setString("p_apellidos", usuario.getApellidos());
            cmd.setString("p_correo", usuario.getCorreo());
            cmd.setString("p_contrasena", usuario.getContrasena());
            cmd.setString("p_dni", usuario.getDni());
            cmd.setBoolean("p_activo", usuario.estaActivo());
            cmd.setInt("p_id", usuario.getId());

            cmd.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {

        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }

        String sql = "{call " + getProcedimientoEliminar() + "(?)}";

        try (
            ConnectionScope scope = ConnectionScope.openForRead();
            CallableStatement cmd = scope.connection().prepareCall(sql)
        ) {

            cmd.setInt("p_id", id);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo eliminar el usuario");
            }
        }
    }
}
