package pe.edu.pucp.accesodatos.dao.usuarios;

import pe.edu.pucp.accesodatos.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Usuario;

import java.sql.SQLException;

public interface UsuarioDAO<T extends Usuario> extends DAO<T, Integer> {

    T findByCorreo(String correo) throws SQLException;

    T findByDni(String dni) throws SQLException;
}
