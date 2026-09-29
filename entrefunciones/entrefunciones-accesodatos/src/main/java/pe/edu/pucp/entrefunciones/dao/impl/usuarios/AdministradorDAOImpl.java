package pe.edu.pucp.entrefunciones.dao.impl.usuarios;

import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.accesodatos.dao.usuarios.AdministradorDAO;

public class AdministradorDAOImpl
        extends UsuarioDAOImpl<Administrador>
        implements AdministradorDAO {

    @Override
    protected Administrador construir(ResultSet rs) throws SQLException {

        Administrador administrador = new Administrador(
                rs.getInt("id"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("correo"),
                rs.getString("contrasena"),
                rs.getString("dni"),
                rs.getString("codigo_empleado")
        );

        if (!rs.getBoolean("activo")) {
            administrador.desactivar();
        }

        return administrador;
    }

    @Override
    protected String getProcedimientoListar() {
        return "listar_administradores";
    }

    @Override
    protected String getProcedimientoBuscarPorId() {
        return "buscar_administrador_por_id";
    }

    @Override
    protected String getProcedimientoBuscarPorCorreo() {
        return "buscar_administrador_por_correo";
    }

    @Override
    protected String getProcedimientoBuscarPorDni() {
        return "buscar_administrador_por_dni";
    }

    @Override
    protected String getProcedimientoInsertar() {
        return "insertar_administrador";
    }

    @Override
    protected String getProcedimientoModificar() {
        return "modificar_administrador";
    }

    @Override
    protected String getProcedimientoEliminar() {
        return "eliminar_administrador";
    }

    @Override
    public Administrador findByCodigoEmpleado(String codigoEmpleado)
            throws SQLException {

        if (codigoEmpleado == null) {
            throw new IllegalArgumentException(
                    "El código de empleado no puede ser nulo"
            );
        }

        String sql = "{call buscar_administrador_por_codigo(?)}";

        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)
        ) {

            cmd.setString("p_codigo_empleado", codigoEmpleado);

            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? construir(rs) : null;
            }
        }
    }

    @Override
    public void insert(Administrador administrador)
            throws SQLException {

        if (administrador == null) {
            throw new IllegalArgumentException(
                    "El administrador no puede ser nulo"
            );
        }

        String sql =
                "{call insertar_administrador(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)
        ) {

            cmd.setString("p_nombres", administrador.getNombres());
            cmd.setString("p_apellidos", administrador.getApellidos());
            cmd.setString("p_correo", administrador.getCorreo());
            cmd.setString("p_contrasena", administrador.getContrasena());
            cmd.setString("p_dni", administrador.getDni());
            cmd.setBoolean("p_activo", administrador.estaActivo());
            cmd.setString(
                    "p_codigo_empleado",
                    administrador.getCodigoEmpleado()
            );

            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo insertar el administrador"
                );
            }

            administrador.setId(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(Administrador administrador)
            throws SQLException {

        if (administrador == null) {
            throw new IllegalArgumentException(
                    "El administrador no puede ser nulo"
            );
        }

        String sql =
                "{call modificar_administrador(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (
            Connection conn = DBManager.getInstance().getConnection();
            CallableStatement cmd = conn.prepareCall(sql)
        ) {

            cmd.setString("p_nombres", administrador.getNombres());
            cmd.setString("p_apellidos", administrador.getApellidos());
            cmd.setString("p_correo", administrador.getCorreo());
            cmd.setString("p_contrasena", administrador.getContrasena());
            cmd.setString("p_dni", administrador.getDni());
            cmd.setBoolean("p_activo", administrador.estaActivo());
            cmd.setString(
                    "p_codigo_empleado",
                    administrador.getCodigoEmpleado()
            );
            cmd.setInt("p_id", administrador.getId());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo actualizar el administrador"
                );
            }
        }
    }
}
