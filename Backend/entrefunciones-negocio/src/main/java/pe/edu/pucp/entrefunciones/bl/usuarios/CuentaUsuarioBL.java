package pe.edu.pucp.entrefunciones.bl.usuarios;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Usuario;

public interface CuentaUsuarioBL {
    //Devuelve el usuario si correo y contraseña son correctos y está activo
    Usuario autenticar(String correo, String contrasena) throws BLException;

    // Busca la cuenta (admin o cliente) por correo.
    Usuario findByCorreo(String correo) throws BLException;

    // cambia la contraseña verificando primero la actual.
    void cambiarContrasena(int idUsuario, String actual, String nueva) throws BLException;

    // pone una contraseña nueva si el correo y el DNI coinciden.
    void recuperarContrasena(String correo, String dni, String nueva) throws BLException;

}
