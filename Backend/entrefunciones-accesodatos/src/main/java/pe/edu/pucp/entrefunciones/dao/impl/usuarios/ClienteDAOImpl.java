package pe.edu.pucp.entrefunciones.dao.impl.usuarios;

import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.dao.usuarios.ClienteDAO;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class ClienteDAOImpl
        extends UsuarioDAOImpl<Cliente>
        implements ClienteDAO {

    @Override
    protected Cliente construir(ResultSet rs) throws SQLException {

        Cliente cliente = new Cliente(
                rs.getInt("id"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("correo"),
                rs.getString("contrasena"),
                rs.getString("dni"),
                rs.getDate("fecha_registro").toLocalDate(),
                rs.getString("telefono")
        );

        if (!rs.getBoolean("activo")) {
            cliente.desactivar();
        }

        return cliente;
    }

    @Override
    protected String getProcedimientoListar() {
        return "listar_clientes";
    }

    @Override
    protected String getProcedimientoBuscarPorId() {
        return "buscar_cliente_por_id";
    }

    @Override
    protected String getProcedimientoBuscarPorCorreo() {
        return "buscar_cliente_por_correo";
    }

    @Override
    protected String getProcedimientoBuscarPorDni() {
        return "buscar_cliente_por_dni";
    }

    @Override
    protected String getProcedimientoInsertar() {
        return "insertar_cliente";
    }

    @Override
    protected String getProcedimientoModificar() {
        return "modificar_cliente";
    }

    @Override
    protected String getProcedimientoEliminar() {
        return "eliminar_cliente";
    }

    @Override
    public void insert(Cliente cliente) throws SQLException {

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "El cliente no puede ser nulo"
            );
        }

        String sql =
                "{call insertar_cliente(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (
            ConnectionScope scope = ConnectionScope.openForRead();
            CallableStatement cmd = scope.connection().prepareCall(sql)
        ) {

            cmd.setString("p_nombres", cliente.getNombres());
            cmd.setString("p_apellidos", cliente.getApellidos());
            cmd.setString("p_correo", cliente.getCorreo());
            cmd.setString("p_contrasena", cliente.getContrasena());
            cmd.setString("p_dni", cliente.getDni());
            cmd.setBoolean("p_activo", cliente.estaActivo());
            cmd.setDate(
                    "p_fecha_registro",
                    Date.valueOf(cliente.getFechaRegistro())
            );
            cmd.setString("p_telefono", cliente.getTelefono());

            cmd.registerOutParameter("p_id", Types.INTEGER);

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo insertar el cliente"
                );
            }

            cliente.setId(cmd.getInt("p_id"));
        }
    }

    @Override
    public void update(Cliente cliente) throws SQLException {

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "El cliente no puede ser nulo"
            );
        }

        String sql =
                "{call modificar_cliente(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (
            ConnectionScope scope = ConnectionScope.openForRead();
            CallableStatement cmd = scope.connection().prepareCall(sql)
        ) {

            cmd.setString("p_nombres", cliente.getNombres());
            cmd.setString("p_apellidos", cliente.getApellidos());
            cmd.setString("p_correo", cliente.getCorreo());
            cmd.setString("p_contrasena", cliente.getContrasena());
            cmd.setString("p_dni", cliente.getDni());
            cmd.setBoolean("p_activo", cliente.estaActivo());
            cmd.setDate(
                    "p_fecha_registro",
                    Date.valueOf(cliente.getFechaRegistro())
            );
            cmd.setString("p_telefono", cliente.getTelefono());
            cmd.setInt("p_id", cliente.getId());

            if (cmd.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo actualizar el cliente"
                );
            }
        }
    }
}
