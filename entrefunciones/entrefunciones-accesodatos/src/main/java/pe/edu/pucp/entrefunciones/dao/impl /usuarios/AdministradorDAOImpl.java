package pe.edu.pucp.accesodatos.dao.impl.usuarios;

import pe.edu.pucp.accesodatos.dao.usuarios.AdministradorDAO;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdministradorDAOImpl
        extends UsuarioDAOImpl<Administrador>
        implements AdministradorDAO {

    @Override
    protected Administrador construir(ResultSet rs)
            throws SQLException {

        Administrador administrador =
                new Administrador(
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
        return "{call listar_administradores()}";
    }

    @Override
    protected String getProcedimientoBuscarPorId() {
        return "{call buscar_administrador_por_id(?)}";
    }

    @Override
    protected String getProcedimientoInsertar() {
        return "{call insertar_administrador(?, ?, ?, ?, ?, ?, ?, ?)}";
    }

    @Override
    protected String getProcedimientoModificar() {
        return "{call modificar_administrador(?, ?, ?, ?, ?, ?, ?, ?)}";
    }

    @Override
    protected String getProcedimientoEliminar() {
        return "{call eliminar_administrador(?)}";
    }

    @Override
    protected void completarInsert(
            CallableStatement cmd,
            Administrador administrador
    ) throws SQLException {

        cmd.setString(
                "p_codigo_empleado",
                administrador.getCodigoEmpleado()
        );
    }

    @Override
    protected void completarUpdate(
            CallableStatement cmd,
            Administrador administrador
    ) throws SQLException {

        cmd.setString(
                "p_codigo_empleado",
                administrador.getCodigoEmpleado()
        );
    }

    @Override
    public Administrador findByCodigoEmpleado(
            String codigoEmpleado
    ) throws SQLException {

        if (codigoEmpleado == null) {
            throw new IllegalArgumentException(
                    "El código de empleado no puede ser nulo"
            );
        }

        String sql =
                "{call buscar_administrador_por_codigo(?)}";

        try (
                var conn = pe.edu.pucp.entrefunciones.db.DBManager
                        .getInstance()
                        .getConnection();

                var cmd = conn.prepareCall(sql)
        ) {

            cmd.setString(
                    "p_codigo_empleado",
                    codigoEmpleado
            );

            try (var rs = cmd.executeQuery()) {

                if (rs.next()) {
                    return construir(rs);
                }

                return null;
            }
        }
    }
}
