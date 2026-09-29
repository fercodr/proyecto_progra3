package pe.edu.pucp.entrefunciones.dao.impl.usuarios;

import pe.edu.pucp.entrefunciones.dao.usuarios.ClienteDAO;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClienteDAOImpl
        extends UsuarioDAOImpl<Cliente>
        implements ClienteDAO {

    @Override
    protected Cliente construir(ResultSet rs)
            throws SQLException {

        Cliente cliente =
                new Cliente(
                        rs.getInt("id"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("correo"),
                        rs.getString("contrasena"),
                        rs.getString("dni"),
                        rs.getDate("fecha_registro")
                                .toLocalDate(),
                        rs.getString("telefono")
                );

        if (!rs.getBoolean("activo")) {
            cliente.desactivar();
        }

        return cliente;
    }

    @Override
    protected String getProcedimientoListar() {
        return "{call listar_clientes()}";
    }

    @Override
    protected String getProcedimientoBuscarPorId() {
        return "{call buscar_cliente_por_id(?)}";
    }

    @Override
    protected String getProcedimientoInsertar() {
        return "{call insertar_cliente(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
    }

    @Override
    protected String getProcedimientoModificar() {
        return "{call modificar_cliente(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
    }

    @Override
    protected String getProcedimientoEliminar() {
        return "{call eliminar_cliente(?)}";
    }

    @Override
    protected void completarInsert(
            CallableStatement cmd,
            Cliente cliente
    ) throws SQLException {

        cmd.setDate(
                "p_fecha_registro",
                Date.valueOf(cliente.getFechaRegistro())
        );

        cmd.setString(
                "p_telefono",
                cliente.getTelefono()
        );
    }

    @Override
    protected void completarUpdate(
            CallableStatement cmd,
            Cliente cliente
    ) throws SQLException {

        cmd.setDate(
                "p_fecha_registro",
                Date.valueOf(cliente.getFechaRegistro())
        );

        cmd.setString(
                "p_telefono",
                cliente.getTelefono()
        );
    }
}
