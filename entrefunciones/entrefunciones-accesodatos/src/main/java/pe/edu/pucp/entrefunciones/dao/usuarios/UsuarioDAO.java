package pe.edu.pucp.entrefunciones.dao.usuarios;

import pe.edu.pucp.entrefunciones.modelo.usuarios.Usuario;

import java.sql.SQLException;


public interface UsuarioDAO extends DAO<Usuario, Integer> {
    Usuario findByCorreo(String correo) throws SQLException;
    Usuario findByDni(String dni) throws SQLException;
}
