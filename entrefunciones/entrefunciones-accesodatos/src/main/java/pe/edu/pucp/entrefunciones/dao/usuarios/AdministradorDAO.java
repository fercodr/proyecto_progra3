package pe.edu.pucp.accesodatos.dao.usuarios;

import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

import java.sql.SQLException;

public interface AdministradorDAO
        extends UsuarioDAO<Administrador> {

    Administrador findByCodigoEmpleado(
            String codigoEmpleado
    ) throws SQLException;
}
